package com.alibaba.cloud.seata.web.reactive;

import io.seata.common.util.StringUtils;
import io.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 *
 * @author yangfengwei
 */
public class ReactiveSeataHandlerFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(ReactiveSeataHandlerFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String xid = RootContext.getXID();
        String rpcXid = exchange.getRequest().getHeaders().getFirst(RootContext.KEY_XID);
        if (log.isDebugEnabled()) {
            log.debug("xid in RootContext {} xid in RpcContext {}", xid, rpcXid);
        }
        if (StringUtils.isBlank(xid) && rpcXid != null) {
            RootContext.bind(rpcXid);
            if (log.isDebugEnabled()) {
                log.debug("bind {} to RootContext", rpcXid);
            }
        }
        Mono<Void> mono = chain.filter(exchange);
        return mono.then(Mono.defer(() -> {
            if (StringUtils.isNotBlank(RootContext.getXID())) {
                if (StringUtils.isNotEmpty(rpcXid)) {
                    String unbindXid = RootContext.unbind();
                    if (log.isDebugEnabled()) {
                        log.debug("unbind {} from RootContext", unbindXid);
                    }
                    if (!rpcXid.equalsIgnoreCase(unbindXid)) {
                        log.warn("xid in change during RPC from {} to {}", rpcXid, unbindXid);
                        if (unbindXid != null) {
                            RootContext.bind(unbindXid);
                            log.warn("bind {} back to RootContext", unbindXid);
                        }
                    }
                }
            }
            return Mono.empty();
        }));

    }
}
