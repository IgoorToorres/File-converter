package com.igor.fileconverter.infrastructure.converter;

import com.igor.fileconverter.domain.converter.ConversionRequest;
import com.igor.fileconverter.domain.converter.ConversionResult;
import com.igor.fileconverter.domain.converter.FileConverter;
import com.igor.fileconverter.domain.enums.FileFormat;
import com.igor.fileconverter.domain.exception.ConversionProcessingException;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class DocxToPdfConverter implements FileConverter {
    private final LibreOfficeCommandExecutor libreOfficeCommandExecutor;

    public DocxToPdfConverter(LibreOfficeCommandExecutor libreOfficeCommandExecutor) {
        this.libreOfficeCommandExecutor = libreOfficeCommandExecutor;
    }

    @Override
    public boolean supports(FileFormat sourceFormat, FileFormat targetFormat) {
        return sourceFormat == FileFormat.DOCX && targetFormat == FileFormat.PDF;
    }

    @Override
    public ConversionResult convert(ConversionRequest request) {
        validateRequest(request);

        libreOfficeCommandExecutor.convertToPdf(
                request.inputFile(),
                request.outputDirectory()
        );

        Path outputFile = resolveOutputFile(request.inputFile(), request.outputDirectory());

        if (!Files.exists(outputFile)) {
            throw new ConversionProcessingException("Arquivo convertido não foi gerado");
        }

        return new ConversionResult(outputFile);
    }

    private void validateRequest(ConversionRequest request) {
        if (request == null) {
            throw new ConversionProcessingException("Requisição de conversão é obrigatória");
        }

        if (!supports(request.sourceFormat(), request.targetFormat())) {
            throw new ConversionProcessingException("Conversor DOCX para PDF não suporta os formatos informados");
        }
    }

    private Path resolveOutputFile(Path inputFile, Path outputDirectory) {
        String inputFileName = inputFile.getFileName().toString();
        int lastDotIndex = inputFileName.lastIndexOf('.');

        String outputFileName = inputFileName.substring(0, lastDotIndex) + ".pdf";

        return outputDirectory.resolve(outputFileName);
    }
}
