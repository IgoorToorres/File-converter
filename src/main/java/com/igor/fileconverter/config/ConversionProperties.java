package com.igor.fileconverter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file-converter.conversion")
public class ConversionProperties {

    private String libreOfficeCommand = "libreoffice";

    public String getLibreOfficeCommand() {
        return libreOfficeCommand;
    }

    public void setLibreOfficeCommand(String libreOfficeCommand) {
        this.libreOfficeCommand = libreOfficeCommand;
    }
}
