package com.igor.fileconverter.domain.converter;

import com.igor.fileconverter.domain.enums.FileFormat;
import com.igor.fileconverter.domain.exception.UnsupportedConversionException;

import java.util.List;
import java.util.Objects;

public class ConverterRegistry {

    private final List<FileConverter> converters;

    public ConverterRegistry(List<FileConverter> converters) {
        Objects.requireNonNull(converters, "Lista de conversores é obrigatória");
        this.converters = List.copyOf(converters);
    }

    public FileConverter findConverter(FileFormat sourceFormat, FileFormat targetFormat) {
        Objects.requireNonNull(sourceFormat, "Formato de origem é obrigatório");
        Objects.requireNonNull(targetFormat, "Formato de destino é obrigatório");

        return converters.stream()
                .filter(converter -> converter.supports(sourceFormat, targetFormat))
                .findFirst()
                .orElseThrow(() -> new UnsupportedConversionException(
                        "Conversão de " + sourceFormat + " para " + targetFormat + " não suportada"
                ));
    }
}
