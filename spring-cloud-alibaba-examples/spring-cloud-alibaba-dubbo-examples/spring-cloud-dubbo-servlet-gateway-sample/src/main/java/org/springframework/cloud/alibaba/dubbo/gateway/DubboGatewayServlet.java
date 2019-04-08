//package org.springframework.cloud.alibaba.dubbo.gateway;
//
//import org.apache.commons.lang.StringUtils;
//import org.apache.dubbo.rpc.service.GenericException;
//import org.apache.dubbo.rpc.service.GenericService;
//import org.springframework.cloud.alibaba.dubbo.http.MutableHttpServerRequest;
//import org.springframework.cloud.alibaba.dubbo.metadata.DubboServiceMetadata;
//import org.springframework.cloud.alibaba.dubbo.metadata.DubboTransportedMetadata;
//import org.springframework.cloud.alibaba.dubbo.metadata.RequestMetadata;
//import org.springframework.cloud.alibaba.dubbo.metadata.RestMethodMetadata;
//import org.springframework.cloud.alibaba.dubbo.metadata.repository.DubboServiceMetadataRepository;
//import org.springframework.cloud.alibaba.dubbo.service.DubboGenericServiceExecutionContext;
//import org.springframework.cloud.alibaba.dubbo.service.DubboGenericServiceExecutionContextFactory;
//import org.springframework.cloud.alibaba.dubbo.service.DubboGenericServiceFactory;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpRequest;
//import org.springframework.util.AntPathMatcher;
//import org.springframework.util.CollectionUtils;
//import org.springframework.util.PathMatcher;
//import org.springframework.util.StreamUtils;
//import org.springframework.web.util.UriComponents;
//
//import javax.servlet.ServletException;
//import javax.servlet.ServletInputStream;
//import javax.servlet.annotation.WebServlet;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.*;
//
//import static org.springframework.web.util.UriComponentsBuilder.fromUriString;
//
//@WebServlet(urlPatterns = "/dsc/*")
//public class DubboGatewayServlet extends HttpServlet {
//
//    private final DubboServiceMetadataRepository repository;
//
//    private final DubboTransportedMetadata dubboTransportedMetadata;
//
//    private final DubboGenericServiceFactory serviceFactory;
//
//    private final DubboGenericServiceExecutionContextFactory contextFactory;
//
//    private final PathMatcher pathMatcher = new AntPathMatcher();
//
//    public DubboGatewayServlet(DubboServiceMetadataRepository repository,
//                               DubboGenericServiceFactory serviceFactory,
//                               DubboGenericServiceExecutionContextFactory contextFactory) {
//        this.repository = repository;
//        this.dubboTransportedMetadata = new DubboTransportedMetadata();
//        dubboTransportedMetadata.setProtocol("dubbo");
//        dubboTransportedMetadata.setCluster("failover");
//        this.serviceFactory = serviceFactory;
//        this.contextFactory = contextFactory;
//    }
//
//    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
//
//        // /g/{app-name}/{rest-path}
//        String requestURI = request.getRequestURI();
//        // /g/
//        String servletPath = request.getServletPath();
//
//        String part = StringUtils.substringAfter(requestURI, servletPath);
//
//        String serviceName = StringUtils.substringBetween(part, "/", "/");
//
//        // App name= spring-cloud-alibaba-dubbo-web-provider (127.0.0.1:8080)
//
//        String restPath = StringUtils.substringAfter(part, serviceName);
//
//        // 初始化 serviceName 的 REST 请求元数据
//        repository.initialize(serviceName);
//        // 将 HttpServletRequest 转化为 RequestMetadata
//        RequestMetadata clientMetadata = buildRequestMetadata(request, restPath);
//
//        DubboServiceMetadata dubboServiceMetadata = repository.get(serviceName, clientMetadata);
//
//        if (dubboServiceMetadata == null) {
//            // if DubboServiceMetadata is not found, executes next
//            throw new ServletException("DubboServiceMetadata can't be found!");
//        }
//
//        RestMethodMetadata dubboRestMethodMetadata = dubboServiceMetadata.getRestMethodMetadata();
//
//        GenericService genericService = serviceFactory.create(dubboServiceMetadata, dubboTransportedMetadata);
//
//        // TODO: Get the Request Body from HttpServletRequest
//        byte[] body = getRequestBody(request);
//
//        MutableHttpServerRequest httpServerRequest = new MutableHttpServerRequest(new HttpRequestAdapter(request), body);
//
////        customizeRequest(httpServerRequest, dubboRestMethodMetadata, clientMetadata);
//
//        DubboGenericServiceExecutionContext context = contextFactory.create(dubboRestMethodMetadata, httpServerRequest);
//
//        Object result = null;
//        GenericException exception = null;
//
//        try {
//            result = genericService.$invoke(context.getMethodName(), context.getParameterTypes(), context.getParameters());
//        } catch (GenericException e) {
//            exception = e;
//        }
//        response.getWriter().println(result);
//    }
//
//    private byte[] getRequestBody(HttpServletRequest request) throws IOException {
//        ServletInputStream inputStream = request.getInputStream();
//        return StreamUtils.copyToByteArray(inputStream);
//    }
//
//    private static class HttpRequestAdapter implements HttpRequest {
//
//        private final HttpServletRequest request;
//
//        private HttpRequestAdapter(HttpServletRequest request) {
//            this.request = request;
//        }
//
//        @Override
//        public String getMethodValue() {
//            return request.getMethod();
//        }
//
//        @Override
//        public URI getURI() {
//            try {
//                return new URI(request.getRequestURL().toString() + "?" + request.getQueryString());
//            } catch (URISyntaxException e) {
//                e.printStackTrace();
//            }
//            throw new RuntimeException();
//        }
//
//        @Override
//        public HttpHeaders getHeaders() {
//            return new HttpHeaders();
//        }
//    }
//
////    protected void customizeRequest(MutableHttpServerRequest httpServerRequest,
////                                    RestMethodMetadata dubboRestMethodMetadata, RequestMetadata clientMetadata) {
////
////        RequestMetadata dubboRequestMetadata = dubboRestMethodMetadata.getRequest();
////        String pathPattern = dubboRequestMetadata.getPath();
////
////        Map<String, String> pathVariables = pathMatcher.extractUriTemplateVariables(pathPattern, httpServerRequest.getPath());
////
////        if (!CollectionUtils.isEmpty(pathVariables)) {
////            // Put path variables Map into query parameters Map
////            httpServerRequest.params(pathVariables);
////        }
////
////    }
//
//    private RequestMetadata buildRequestMetadata(HttpServletRequest request, String restPath) {
//        UriComponents uriComponents = fromUriString(request.getRequestURI()).build(true);
//        RequestMetadata requestMetadata = new RequestMetadata();
//        requestMetadata.setPath(restPath);
//        requestMetadata.setMethod(request.getMethod());
//        requestMetadata.setParams(getParams(request));
//        requestMetadata.setHeaders(getHeaders(request));
//        return requestMetadata;
//    }
//
//    private Map<String, List<String>> getHeaders(HttpServletRequest request) {
//        Map<String, List<String>> map = new LinkedHashMap<>();
//        Enumeration<String> headerNames = request.getHeaderNames();
//        while (headerNames.hasMoreElements()) {
//            String headerName = headerNames.nextElement();
//            Enumeration<String> headerValues = request.getHeaders(headerName);
//            map.put(headerName, Collections.list(headerValues));
//        }
//        return map;
//    }
//
//    private Map<String, List<String>> getParams(HttpServletRequest request) {
//        Map<String, List<String>> map = new LinkedHashMap<>();
//        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
//            map.put(entry.getKey(), Arrays.asList(entry.getValue()));
//        }
//        return map;
//    }
//}
