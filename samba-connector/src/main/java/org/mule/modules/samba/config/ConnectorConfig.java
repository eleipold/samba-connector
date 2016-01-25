package org.mule.modules.samba.config;

import org.mule.api.annotations.components.Configuration;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.Optional;
import org.mule.api.annotations.Configurable;

@Configuration(friendlyName = "Configuration")
public class ConnectorConfig {

    @Configurable
    @Optional
    private String domain;

    @Configurable
    private String host;

    @Configurable
    @Optional
    @Default(value="445")
    private int port;
    
    @Configurable
    private String user;
    
    @Configurable
    private String password;
    
    @Configurable
    private String share;

    public String getDomain() {
        return domain;
    }
    public void setDomain(String domain) {
        this.domain = domain;
    }
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getShare() {
        return share;
    }
    public void setShare(String share) {
        this.share = share;
    }
}