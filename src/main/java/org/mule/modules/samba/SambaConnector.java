package org.mule.modules.samba;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

import org.apache.commons.io.IOUtils;
import org.mule.api.annotations.Config;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.Source;
import org.mule.api.annotations.SourceStrategy;
import org.mule.api.annotations.param.Optional;
import org.mule.modules.samba.config.ConnectorConfig;
import org.mule.api.callback.SourceCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Connector(name="samba", friendlyName="Samba")
public class SambaConnector {
    private static Logger logger = LoggerFactory.getLogger(SambaConnector.class);

    @Config
    ConnectorConfig config;

    @Source(sourceStrategy=SourceStrategy.POLLING, pollingPeriod=1000, name="receiver")
    public void receiver(@Optional final String host, final String path, @Optional final String wildcardPattern, final SourceCallback sourceCallback) {
        String url = this.getUrl(host, path);
        logger.debug("Connecting to Samba share: " + url.toString());
        NtlmPasswordAuthentication auth = this.getAuth();
        try {
            SmbFile resource = new SmbFile(url.toString(), auth);
            if (resource.isFile()) {
                this.processFile(resource, sourceCallback);
            } else if (resource.list().length > 0) {
                SmbFile[] file = wildcardPattern == null ? resource.listFiles() : resource.listFiles(wildcardPattern);
                for (int i = 0; i < file.length; i++) {
                    this.processFile(file[i], sourceCallback);
                }
            } else {
                logger.debug("No files to process for " + url.toString());
            }
        } catch (SmbAuthException authException) {
            logger.error("Attempting to login with domain: " + auth.getDomain() + " and user: " + auth.getUsername() + " failed");
            authException.printStackTrace();
        } catch (SmbException e) {
            if (e.getMessage().equals("The system cannot find the file specified.")) {
                logger.debug("A file with the filename pattern '" + wildcardPattern + "'could not be found.");
            } else {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Processor
    public List<byte[]> get(@Optional final String host, final String path, @Optional final String wildcardPattern) {
        List<byte[]> files = new ArrayList<>();
        String url = this.getUrl(host, path);
        logger.debug("Connecting to Samba share: " + url.toString());
        NtlmPasswordAuthentication auth = this.getAuth();
        try {
            SmbFile resource = new SmbFile(url.toString(), auth);
            if (resource.isFile()) {
                files.add(this.getContents(resource));
            } else if (resource.list().length > 0) {
                SmbFile[] file = wildcardPattern == null ? resource.listFiles() : resource.listFiles(wildcardPattern);
                for (int i = 0; i < file.length; i++) {
                    files.add(this.getContents(file[i]));
                }
            } else {
                logger.debug("No files to process for " + url.toString());
            }
        } catch (SmbAuthException authException) {
            logger.error("Attempting to login with domain: " + auth.getDomain() + " and user: " + auth.getUsername() + " failed");
            authException.printStackTrace();
            return null;
        } catch (SmbException e) {
            if (e.getMessage().equals("The system cannot find the file specified.")) {
                logger.debug("A file with the filename pattern '" + wildcardPattern + "'could not be found.");
            } else {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return files;
    }

    private byte[] getContents(SmbFile file) {
        byte[] contents;
        try {
            SmbFileInputStream inFile = new SmbFileInputStream(file);
            contents = IOUtils.toByteArray(inFile);
            inFile.close();
            file.delete();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return contents;
    }
    
    private void processFile(SmbFile file, SourceCallback sourceCallback) throws SmbException {
        try {
            SmbFileInputStream inFile = new SmbFileInputStream(file);
            sourceCallback.process(IOUtils.toString(inFile));
            inFile.close();
            file.delete();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    protected String getUrl(String host, String path) {
        StringBuffer url = new StringBuffer("smb://");
        if (config.getDomain() != null)
            url.append(config.getDomain()).append(";");
        url.append(host == null ? config.getHost() : host).append(":").append(config.getPort());
        if (! config.getShare().startsWith("/"))
            url.append("/");
        url.append(config.getShare());
        if (! path.startsWith("/"))
            url.append("/");
        url.append(path);
        if (! path.endsWith("/"))
            url.append("/");
        return url.toString();
    }
    
    protected NtlmPasswordAuthentication getAuth() {
        return new NtlmPasswordAuthentication(config.getDomain() == null ? "" : config.getDomain(), config.getUser(), config.getPassword());
    }
    
    public ConnectorConfig getConfig() {
        return config;
    }
    public void setConfig(ConnectorConfig config) {
        this.config = config;
    }
}