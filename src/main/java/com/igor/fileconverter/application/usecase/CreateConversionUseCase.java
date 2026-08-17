package com.igor.fileconverter.application.usecase;

import com.igor.fileconverter.application.service.FileStorageService;
import com.igor.fileconverter.domain.entity.Conversion;
import com.igor.fileconverter.domain.enums.FileFormat;
import com.igor.fileconverter.domain.exception.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;

@Service
public class CreateConversionUseCase {
    private final FileStorageService fileStorageService;

    public CreateConversionUseCase(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public Conversion execute(MultipartFile file, FileFormat targetFormat) {
        validate(file);

        if (targetFormat == null) {
            throw new DomainException("Formato de destino é obrigatório");
        }

        String originalFileName = file.getOriginalFilename();
        FileFormat sourceFormat = detectSourceFormat(originalFileName);

        String inputStorageKey = storeFile(file, originalFileName);

        return Conversion.create(
                originalFileName,
                inputStorageKey,
                sourceFormat,
                targetFormat,
                inputStorageKey
        );
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new DomainException("Arquivo é obrigatório");
        }

        if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            throw new DomainException("Nome original do arquivo é obrigatório");
        }
    }

    private FileFormat detectSourceFormat(String originalFileName) {
        String extension = extractExtension(originalFileName);

        return switch (extension) {
            case "docx" -> FileFormat.DOCX;
            case "xlsx" -> FileFormat.XLSX;
            case "pptx" -> FileFormat.PPTX;
            case "pdf" -> FileFormat.PDF;
            default -> throw new DomainException("Formato de arquivo não suportado");
        };
    }

    private String extractExtension(String originalFileName) {
        int lastDotIndex = originalFileName.lastIndexOf('.');

        if (lastDotIndex <= 0 || lastDotIndex == originalFileName.length() - 1) {
            throw new DomainException("Arquivo precisa ter extensão");
        }

        return originalFileName.substring(lastDotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String storeFile(MultipartFile file, String originalFileName) {
        try {
            return fileStorageService.store(originalFileName, file.getInputStream());
        } catch (IOException exception) {
            throw new DomainException("Erro ao ler arquivo enviado");
        }
    }
}
