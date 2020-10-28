package com.alibaba.cloud.nacos.ribbon;

import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.netflix.client.config.CommonClientConfigKey;
import com.netflix.client.config.IClientConfig;
import com.netflix.config.DynamicIntProperty;
import com.netflix.loadbalancer.ServerListUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A server list updater for the
 * {@link com.netflix.loadbalancer.DynamicServerListLoadBalancer} that utilizes nacos's
 * event listener to trigger LB cache updates.
 * <p>
 * Note that when a cache refreshed notification is received, the actual update on the
 * serverList is done on a separate scheduler as the notification is delivered on an
 * namingService thread.
 * <p>
 * Learn from {@link com.netflix.niws.loadbalancer.EurekaNotificationServerListUpdater}
 * and {@link com.netflix.loadbalancer.PollingServerListUpdater},thanks.
 *
 * @author David Liu
 * @author gaowei
 */
public class NacosNotificationServerListUpdater implements ServerListUpdater {

	private static final Logger logger = LoggerFactory
			.getLogger(NacosNotificationServerListUpdater.class);

	private static long LISTOFSERVERS_CACHE_UPDATE_DELAY = 1000; // msecs;
	private static int LISTOFSERVERS_CACHE_REPEAT_INTERVAL = 30 * 1000; // msecs;

	private static class LazyHolder {
		private final static String CORE_THREAD = "NacosNotificationServerListUpdater.ThreadPoolSize";
		private final static String QUEUE_SIZE = "NacosNotificationServerListUpdater.queueSize";
		private final static LazyHolder SINGLETON = new LazyHolder();

		private final DynamicIntProperty poolSizeProp = new DynamicIntProperty(
				CORE_THREAD, 2);
		private final DynamicIntProperty queueSizeProp = new DynamicIntProperty(
				QUEUE_SIZE, 1000);
		private final ThreadPoolExecutor defaultServerListUpdateExecutor;
		private final ScheduledThreadPoolExecutor timedServerListRefreshExecutor;
		private final Thread shutdownThread;

		private LazyHolder() {
			int corePoolSize = getCorePoolSize();
			defaultServerListUpdateExecutor = new ThreadPoolExecutor(corePoolSize,
					corePoolSize * 5, 0, TimeUnit.NANOSECONDS,
					new ArrayBlockingQueue<Runnable>(queueSizeProp.get()),
					new ThreadFactoryBuilder().setNameFormat(
							"NacosNotificationServerListUpdater-defaultServerListUpdateExecutor-%d")
							.setDaemon(true).build());
			ThreadFactory factory = (new ThreadFactoryBuilder()).setNameFormat(
					"NacosNotificationServerListUpdater-timedServerListRefreshExecutor-%d")
					.setDaemon(true).build();
			timedServerListRefreshExecutor = new ScheduledThreadPoolExecutor(corePoolSize,
					factory);
			poolSizeProp.addCallback(new Runnable() {
				@Override
				public void run() {
					int corePoolSize = getCorePoolSize();
					defaultServerListUpdateExecutor.setCorePoolSize(corePoolSize);
					defaultServerListUpdateExecutor.setMaximumPoolSize(corePoolSize * 5);
					timedServerListRefreshExecutor.setCorePoolSize(corePoolSize);
				}
			});

			shutdownThread = new Thread(new Runnable() {
				@Override
				public void run() {
					logger.info(
							"Shutting down the Executor for NacosNotificationServerListUpdater");
					try {
						defaultServerListUpdateExecutor.shutdown();
						timedServerListRefreshExecutor.shutdown();
						Runtime.getRuntime().removeShutdownHook(shutdownThread);
					}
					catch (Exception e) {
						// this can happen in the middle of a real shutdown, and that's
						// ok.
					}
				}
			});

			Runtime.getRuntime().addShutdownHook(shutdownThread);
		}

		private int getCorePoolSize() {
			int propSize = poolSizeProp.get();
			if (propSize > 0) {
				return propSize;
			}
			return 2; // default
		}
	}

	public static ExecutorService getDefaultRefreshExecutor() {
		return LazyHolder.SINGLETON.defaultServerListUpdateExecutor;
	}

	public static ScheduledThreadPoolExecutor getTimedRefreshExecutor() {
		return LazyHolder.SINGLETON.timedServerListRefreshExecutor;
	}

	private final AtomicBoolean updateQueued = new AtomicBoolean(false);
	private final AtomicBoolean isActive = new AtomicBoolean(false);
	private final AtomicLong lastUpdated = new AtomicLong(System.currentTimeMillis());
	private final ExecutorService refreshExecutor;
	private final NacosDiscoveryProperties nacosDiscoveryProperties;
	private final IClientConfig clientConfig;
	private volatile EventListener nacosEventListener;
	private volatile ScheduledFuture<?> scheduledFuture;
	private final long initialDelayMs;
	private final long refreshIntervalMs;

	public NacosNotificationServerListUpdater(
			NacosDiscoveryProperties nacosDiscoveryProperties, IClientConfig config) {
		this(nacosDiscoveryProperties, config, getDefaultRefreshExecutor(),
				LISTOFSERVERS_CACHE_UPDATE_DELAY, getRefreshIntervalMs(config));
	}

	public NacosNotificationServerListUpdater(
			NacosDiscoveryProperties nacosDiscoveryProperties, IClientConfig clientConfig,
			ExecutorService refreshExecutor, final long initialDelayMs,
			final long refreshIntervalMs) {
		this.nacosDiscoveryProperties = nacosDiscoveryProperties;
		this.clientConfig = clientConfig;
		this.refreshExecutor = refreshExecutor;
		this.initialDelayMs = initialDelayMs;
		this.refreshIntervalMs = refreshIntervalMs;
	}

	@Override
	public synchronized void start(final UpdateAction updateAction) {
		if (isActive.compareAndSet(false, true)) {
			NamingService namingService = nacosDiscoveryProperties
					.namingServiceInstance();
			this.nacosEventListener = new EventListener() {
				@Override
				public void onEvent(Event event) {
					if (event instanceof NamingEvent) {
						if (!updateQueued.compareAndSet(false, true)) { // if an update is
							// already queued
							logger.info(
									"an update action is already queued, returning as no-op");
							return;
						}
						logger.info(
								"Trigger Nacos change event and received a message ，serviceName:{}",
								((NamingEvent) event).getServiceName());
						if (!refreshExecutor.isShutdown()) {
							try {
								refreshExecutor.submit(new Runnable() {
									@Override
									public void run() {
										try {
											updateAction.doUpdate();
											lastUpdated.set(System.currentTimeMillis());
										}
										catch (Exception e) {
											logger.warn("Failed to update serverList", e);
										}
										finally {
											updateQueued.set(false);
										}
									}
								}); // fire and forget
							}
							catch (Exception e) {
								logger.warn(
										"Error submitting update task to executor, skipping one round of updates",
										e);
								updateQueued.set(false); // if submit fails, need to reset
								// updateQueued to false
							}
						}
						else {
							logger.debug(
									"stopping NacosNotificationServerListUpdater, as refreshExecutor has been shut down");
							stop();
						}
					}
				}
			};
			try {
				namingService.subscribe(clientConfig.getClientName(), nacosEventListener);
			}
			catch (NacosException e) {
				logger.warn(
						"Error submitting update task to executor, skipping one round of updates",
						e);
				updateQueued.set(false); // if submit fails, need to reset updateQueued to
				// false
			}
			scheduledFuture = getTimedRefreshExecutor()
					.scheduleWithFixedDelay(new Runnable() {
						@Override
						public void run() {
							if (!isActive.get()) {
								if (scheduledFuture != null) {
									scheduledFuture.cancel(true);
								}
								return;
							}
							if (!updateQueued.compareAndSet(false, true)) { // if an
								// update is
								// already
								// queued
								logger.info(
										"an update action is already queued, returning as no-op");
								return;
							}
							try {
								updateAction.doUpdate();
								lastUpdated.set(System.currentTimeMillis());
							}
							catch (Exception e) {
								logger.warn("Failed one update cycle", e);
							}
							finally {
								updateQueued.set(false);
							}
						}
					}, initialDelayMs, refreshIntervalMs, TimeUnit.MILLISECONDS);
		}
		else {
			logger.info("Already active, no-op");
		}
	}

	@Override
	public synchronized void stop() {
		if (isActive.compareAndSet(true, false)) {
			NamingService namingService = nacosDiscoveryProperties
					.namingServiceInstance();
			if (namingService != null) {
				try {
					namingService.unsubscribe(clientConfig.getClientName(),
							nacosEventListener);
				}
				catch (NacosException e) {
					e.printStackTrace();
				}
			}
			if (scheduledFuture != null) {
				scheduledFuture.cancel(true);
			}
		}
		else {
			logger.info("Not currently active, no-op");
		}
	}

	@Override
	public String getLastUpdate() {
		return new Date(lastUpdated.get()).toString();
	}

	@Override
	public long getDurationSinceLastUpdateMs() {
		return System.currentTimeMillis() - lastUpdated.get();
	}

	@Override
	public int getNumberMissedCycles() {
		if (!isActive.get()) {
			return 0;
		}
		return (int) ((int) (System.currentTimeMillis() - lastUpdated.get())
				/ refreshIntervalMs);
	}

	@Override
	public int getCoreThreads() {
		if (isActive.get()) {
			if (refreshExecutor != null
					&& refreshExecutor instanceof ThreadPoolExecutor) {
				return ((ThreadPoolExecutor) refreshExecutor).getCorePoolSize();
			}
		}
		return 0;
	}

	private static long getRefreshIntervalMs(IClientConfig clientConfig) {
		return clientConfig.get(CommonClientConfigKey.ServerListRefreshInterval,
				LISTOFSERVERS_CACHE_REPEAT_INTERVAL);
	}
}
