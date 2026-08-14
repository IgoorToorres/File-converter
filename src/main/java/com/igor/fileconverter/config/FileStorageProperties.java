package com.igor.fileconverter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file-converter.storage")
public class FileStorageProperties {

    private String rootDirectory = "storage";

    public String getRootDirectory(){
        return rootDirectory;
    }

    public void setRootDirectory(String rootDirectory){
        this.rootDirectory = rootDirectory;
    }

}
