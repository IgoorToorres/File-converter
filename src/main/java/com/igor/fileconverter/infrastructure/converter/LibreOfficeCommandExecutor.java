package com.igor.fileconverter.infrastructure.converter;


import java.nio.file.Path;

public interface LibreOfficeCommandExecutor {
    void convertToPdf(Path inputFile, Path outputDirectory);
}
