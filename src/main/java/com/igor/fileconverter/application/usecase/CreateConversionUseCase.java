package com.igor.fileconverter.application.usecase;

import com.igor.fileconverter.application.service.FileFormatDetector;
import com.igor.fileconverter.application.service.FileMimeTypeValidator;
import com.igor.fileconverter.application.service.FileStorageService;
import com.igor.fileconverter.application.service.SupportedConversionPolicy;
import com.igor.fileconverter.domain.converter.ConversionRequest;
import com.igor.fileconverter.domain.converter.ConversionResult;
import com.igor.fileconverter.domain.converter.ConverterRegistry;
import com.igor.fileconverter.domain.converter.FileConverter;
import com.igor.fileconverter.domain.entity.Conversion;
import com.igor.fileconverter.domain.enums.FileFormat;
import com.igor.fileconverter.domain.exception.ConversionProcessingException;
import com.igor.fileconverter.domain.exception.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

@Service
public class CreateConversionUseCase {
    private final FileStorageService fileStorageService;
    private final FileFormatDetector fileFormatDetector;
    private final FileMimeTypeValidator fileMimeTypeValidator;
    private final SupportedConversionPolicy supportedConversionPolicy;
    private final ConverterRegistry converterRegistry;

    public CreateConversionUseCase(
            FileStorageService fileStorageService,
            FileFormatDetector fileFormatDetector,
            FileMimeTypeValidator fileMimeTypeValidator,
            SupportedConversionPolicy supportedConversionPolicy,
            ConverterRegistry converterRegistry
    ) {
        this.fileStorageService = fileStorageService;
        this.fileFormatDetector = fileFormatDetector;
        this.fileMimeTypeValidator = fileMimeTypeValidator;
        this.supportedConversionPolicy = supportedConversionPolicy;
        this.converterRegistry = converterRegistry;
    }

    public Conversion execute(MultipartFile file, FileFormat targetFormat) {
        validate(file);

        if (targetFormat == null) {
            throw new DomainException("Formato de destino é obrigatório");
        }

        String originalFileName = file.getOriginalFilename();
        FileFormat sourceFormat = fileFormatDetector.detect(originalFileName);
        fileMimeTypeValidator.validate(file, sourceFormat);
        supportedConversionPolicy.validate(sourceFormat, targetFormat);

        String inputStorageKey = storeFile(file, originalFileName);
        Path inputFile = fileStorageService.load(inputStorageKey);

        Conversion conversion =  Conversion.create(
                originalFileName,
                inputStorageKey,
                sourceFormat,
                targetFormat,
                inputStorageKey
        );

        conversion.startProcessing();
        Path tempDirectory = createTempDirectory();

        try {
            FileConverter converter = converterRegistry.findConverter(sourceFormat, targetFormat);

            ConversionRequest request = new ConversionRequest(
                    inputFile,
                    tempDirectory,
                    sourceFormat,
                    targetFormat
            );

            ConversionResult result = converter.convert(request);

            String outputFileName = buildOutputFileName(originalFileName, targetFormat);
            String outputStorageKey = fileStorageService.store(outputFileName, result.outputFile());

            conversion.complete(outputStorageKey);

            return conversion;
        } catch (RuntimeException exception) {
            conversion.fail(exception.getMessage());
            throw exception;
        } finally {
            deleteRecursively(tempDirectory);
        }

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

    private Path createTempDirectory() {
        try {
            return Files.createTempDirectory("file-converter-");
        } catch (IOException exception) {
            throw new ConversionProcessingException("Erro ao criar diretório temporário", exception);
        }
    }

    private void deleteRecursively(Path directory) {
        if (directory == null || !Files.exists(directory)) {
            return;
        }

        try (var paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException exception) {
                            throw new ConversionProcessingException("Erro ao limpar diretório temporário", exception);
                        }
                    });
        } catch (IOException exception) {
            throw new ConversionProcessingException("Erro ao limpar diretório temporário", exception);
        }
    }

    private String buildOutputFileName(String originalFileName, FileFormat targetFormat) {
        int lastDotIndex = originalFileName.lastIndexOf('.');

        String baseName = lastDotIndex > 0
                ? originalFileName.substring(0, lastDotIndex)
                : originalFileName;

        return baseName + "." + targetFormat.name().toLowerCase();
    }
}
