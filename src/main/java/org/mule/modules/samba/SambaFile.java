package org.mule.modules.samba;

import java.io.Serializable;

public class SambaFile implements Serializable {
    private static final long serialVersionUID = 4619536790644892249L;

    private String filename;
    private byte[] content;
    
    public String getFilename() {
        return filename;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }
    public byte[] getContent() {
        return content;
    }
    public void setContent(byte[] content) {
        this.content = content;
    }
}
