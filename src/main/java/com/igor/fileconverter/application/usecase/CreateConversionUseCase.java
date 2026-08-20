package com.igor.fileconverter.application.usecase;

import com.igor.fileconverter.application.service.FileFormatDetector;
import com.igor.fileconverter.application.service.FileStorageService;
import com.igor.fileconverter.application.service.SupportedConversionPolicy;
import com.igor.fileconverter.domain.entity.Conversion;
import com.igor.fileconverter.domain.enums.FileFormat;
import com.igor.fileconverter.domain.exception.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class CreateConversionUseCase {
    private final FileStorageService fileStorageService;
    private final FileFormatDetector fileFormatDetector;
    private final SupportedConversionPolicy supportedConversionPolicy;

    public CreateConversionUseCase(
            FileStorageService fileStorageService,
            FileFormatDetector fileFormatDetector,
            SupportedConversionPolicy supportedConversionPolicy
    ) {
        this.fileStorageService = fileStorageService;
        this.fileFormatDetector = fileFormatDetector;
        this.supportedConversionPolicy = supportedConversionPolicy;
    }

    public Conversion execute(MultipartFile file, FileFormat targetFormat) {
        validate(file);

        if (targetFormat == null) {
            throw new DomainException("Formato de destino é obrigatório");
        }

        String originalFileName = file.getOriginalFilename();
        FileFormat sourceFormat = fileFormatDetector.detect(originalFileName);
        supportedConversionPolicy.validate(sourceFormat, targetFormat);

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

    private String storeFile(MultipartFile file, String originalFileName) {
        try {
            return fileStorageService.store(originalFileName, file.getInputStream());
        } catch (IOException exception) {
            throw new DomainException("Erro ao ler arquivo enviado");
        }
    }
}
