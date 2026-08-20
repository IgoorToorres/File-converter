package com.igor.fileconverter.application.service;

import com.igor.fileconverter.domain.enums.FileFormat;
import com.igor.fileconverter.domain.exception.UnsupportedConversionException;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DefaultSupportedConversionPolicy implements SupportedConversionPolicy {

    private static final Set<FileFormat> OFFICE_FORMATS = Set.of(
            FileFormat.DOCX,
            FileFormat.XLSX,
            FileFormat.PPTX
    );

    @Override
    public void validate(FileFormat sourceFormat, FileFormat targetFormat) {
        if (sourceFormat == null) {
            throw new UnsupportedConversionException("Formato de origem é obrigatório");
        }

        if (targetFormat == null) {
            throw new UnsupportedConversionException("Formato de destino é obrigatório");
        }

        if (sourceFormat == targetFormat) {
            throw new UnsupportedConversionException("Formato de origem e destino não podem ser iguais");
        }

        if (targetFormat == FileFormat.PDF && OFFICE_FORMATS.contains(sourceFormat)) {
            return;
        }

        throw new UnsupportedConversionException(
                "Conversão de " + sourceFormat + " para " + targetFormat + " não suportada"
        );
    }
}
