package org.endeavourhealth.coreui.framework.config.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthConfigRaw {
    private String authServerUrl;
    private String realm;
    private String resource;

    @JsonProperty("auth-server-url")
    public String getAuthServerUrl() {
        return authServerUrl;
    }

    @JsonProperty("auth-server-url")
    public AuthConfigRaw setAuthServerUrl(String authServerUrl) {
        this.authServerUrl = authServerUrl;
        return this;
    }

    public String getRealm() {
        return realm;
    }

    public AuthConfigRaw setRealm(String realm) {
        this.realm = realm;
        return this;
    }

    public String getResource() {
        return resource;
    }

    public AuthConfigRaw setResource(String resource) {
        this.resource = resource;
        return this;
    }
}
