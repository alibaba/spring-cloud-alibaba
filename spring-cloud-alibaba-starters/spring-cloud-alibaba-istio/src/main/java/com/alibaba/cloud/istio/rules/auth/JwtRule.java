package com.alibaba.cloud.istio.rules.auth;

import io.envoyproxy.envoy.extensions.filters.http.jwt_authn.v3.JwtHeader;

import java.util.List;

public class JwtRule {
    private String name;
    private List<JwtHeader> fromHeaders;
    private String issuer;
    private List<String> audiences;
    private String jwksUri;
    private String jwks;
    private List<String> fromParams;
    private String outputPayloadToHeader;
    private boolean forwardOriginalToken;

    public JwtRule(String name, List<JwtHeader> fromHeaders, String issuer, List<String> audiences, String jwksUri, String jwks, List<String> fromParams, String outputPayloadToHeader, boolean forwardOriginalToken) {
        this.name = name;
        this.fromHeaders = fromHeaders;
        this.issuer = issuer;
        this.audiences = audiences;
        this.jwksUri = jwksUri;
        this.jwks = jwks;
        this.fromParams = fromParams;
        this.outputPayloadToHeader = outputPayloadToHeader;
        this.forwardOriginalToken = forwardOriginalToken;
    }

    public String getName() {
        return name;
    }

    public List<JwtHeader> getFromHeaders() {
        return fromHeaders;
    }

    public String getIssuer() {
        return issuer;
    }

    public List<String> getAudiences() {
        return audiences;
    }

    public String getJwksUri() {
        return jwksUri;
    }

    public String getJwks() {
        return jwks;
    }

    public List<String> getFromParams() {
        return fromParams;
    }

    public String getOutputPayloadToHeader() {
        return outputPayloadToHeader;
    }

    public boolean isForwardOriginalToken() {
        return forwardOriginalToken;
    }
}
