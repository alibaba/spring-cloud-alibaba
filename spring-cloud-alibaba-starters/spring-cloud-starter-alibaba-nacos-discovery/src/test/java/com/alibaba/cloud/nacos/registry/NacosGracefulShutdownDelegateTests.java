package com.alibaba.cloud.nacos.registry;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:uuuyuqi@gmail.com">uuuyuqi</a>
 */
@ExtendWith(MockitoExtension.class)
public class NacosGracefulShutdownDelegateTests {

    @Mock
    private NacosAutoServiceRegistration autoServiceRegistration;

    @Mock
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private NacosGracefulShutdownDelegate delegate;

    @BeforeEach
    void setUp() {
        delegate.setApplicationContext(applicationContext);
    }

    @Test
    public void sameContextShouldTriggerStop() {
        when(nacosDiscoveryProperties.getGracefulShutdownWaitTime()).thenReturn(0);

        delegate.onApplicationEvent(new ContextClosedEvent(applicationContext));

        verify(autoServiceRegistration).stop();
    }

    @Test
    public void differentContextShouldNotTriggerStop() {
        delegate.onApplicationEvent(new ContextClosedEvent(Mockito.mock(ApplicationContext.class)));

        verify(autoServiceRegistration, never()).stop();
    }

    @Test
    public void stopExceptionShouldBeSwallowed() {
        doThrow(new RuntimeException("test exception")).when(autoServiceRegistration).stop();

        delegate.onApplicationEvent(new ContextClosedEvent(applicationContext));

        verify(autoServiceRegistration).stop();
    }

    @Test
    public void supportsAsyncExecutionShouldBeFalse() {
        assertThat(delegate.supportsAsyncExecution()).isFalse();
    }
}


