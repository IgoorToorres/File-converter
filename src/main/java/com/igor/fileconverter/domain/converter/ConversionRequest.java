package com.igor.fileconverter.domain.converter;

import com.igor.fileconverter.domain.enums.FileFormat;

import java.nio.file.Path;

public record ConversionRequest (
        Path inputFile,
        Path outputDirectory,
        FileFormat sourceFormat,
        FileFormat targetFormat
){
    public ConversionRequest {
        if (inputFile == null) {
            throw new IllegalArgumentException("Arquivo de entrada é obrigatório");
        }

        if (outputDirectory == null) {
            throw new IllegalArgumentException("Diretório de saída é obrigatório");
        }

        if (sourceFormat == null) {
            throw new IllegalArgumentException("Formato de origem é obrigatório");
        }

        if (targetFormat == null) {
            throw new IllegalArgumentException("Formato de destino é obrigatório");
        }
    }
}
