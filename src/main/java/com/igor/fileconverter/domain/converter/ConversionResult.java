package com.igor.fileconverter.domain.converter;

import java.nio.file.Path;

public record ConversionResult(
        Path outputFile
) {
    public ConversionResult {
        if (outputFile == null) {
            throw new IllegalArgumentException("Arquivo convertido é obrigatório");
        }
    }
}
