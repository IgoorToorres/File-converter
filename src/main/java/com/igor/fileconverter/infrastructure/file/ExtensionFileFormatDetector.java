package com.igor.fileconverter.infrastructure.file;

import com.igor.fileconverter.application.service.FileFormatDetector;
import com.igor.fileconverter.domain.enums.FileFormat;
import com.igor.fileconverter.domain.exception.DomainException;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ExtensionFileFormatDetector implements FileFormatDetector {

    @Override
    public FileFormat detect(String originalFileName) {
        validateOriginalFileName(originalFileName);

        String extension = extractExtension(originalFileName);

        return switch (extension) {
            case "docx" -> FileFormat.DOCX;
            case "xlsx" -> FileFormat.XLSX;
            case "pptx" -> FileFormat.PPTX;
            case "pdf" -> FileFormat.PDF;
            default -> throw new DomainException("Formato de arquivo não suportado");
        };
    }

    private void validateOriginalFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new DomainException("Nome original do arquivo é obrigatório");
        }
    }

    private String extractExtension(String originalFileName) {
        int lastDotIndex = originalFileName.lastIndexOf('.');

        if (lastDotIndex <= 0 || lastDotIndex == originalFileName.length() - 1) {
            throw new DomainException("Arquivo precisa ter extensão");
        }

        return originalFileName.substring(lastDotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
