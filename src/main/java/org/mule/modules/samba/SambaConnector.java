package org.mule.modules.samba;

import java.io.IOException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

import org.apache.commons.io.IOUtils;
import org.mule.api.annotations.Config;
import org.mule.api.annotations.Connector;
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

    @Source(sourceStrategy=SourceStrategy.POLLING, pollingPeriod=1000)
    public void receive(String path, @Optional String wildcardPattern, final SourceCallback sourceCallback) {
        StringBuffer url = new StringBuffer("smb://");
        if (config.getDomain() != null)
            url.append(config.getDomain()).append(";");
        url.append(config.getHost()).append(":").append(config.getPort());
        if (! config.getShare().startsWith("/"))
            url.append("/");
        url.append(config.getShare());
        if (! path.startsWith("/"))
            url.append("/");
        url.append(path);
        if (! path.endsWith("/"))
            url.append("/");
        logger.debug("Connecting to Samba share: " + url.toString());
        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(
                config.getDomain() == null ? "" : config.getDomain(), config.getUser(), config.getPassword());
        try {
            SmbFile resource = new SmbFile(url.toString(), auth);
            if (resource.isFile()) {
                SmbFileInputStream inFile = new SmbFileInputStream(resource);
                sourceCallback.process(IOUtils.toString(inFile));
                inFile.close();
                resource.delete();
            } else if (resource.list().length > 0) {
                SmbFile[] file = wildcardPattern == null ? resource.listFiles() : resource.listFiles(wildcardPattern);
                for (int i = 0; i < file.length; i++) {
                    SmbFileInputStream inFile = new SmbFileInputStream(file[i]);
                    sourceCallback.process(IOUtils.toString(inFile));
                    inFile.close();
                    file[i].delete();
                }
            } else {
                logger.debug("No files to process for " + url.toString());
            }
        } catch (SmbException e) {
            if (e.getMessage().equals("The system cannot find the file specified.")) {
                logger.debug("A file with the filename pattern '" + wildcardPattern + "'could not be found.");
            } else {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public ConnectorConfig getConfig() {
        return config;
    }
    public void setConfig(ConnectorConfig config) {
        this.config = config;
    }
}