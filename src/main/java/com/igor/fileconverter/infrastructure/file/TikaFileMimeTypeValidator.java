package com.igor.fileconverter.infrastructure.file;

import com.igor.fileconverter.application.service.FileMimeTypeValidator;
import com.igor.fileconverter.domain.enums.FileFormat;
import com.igor.fileconverter.domain.exception.InvalidFileTypeException;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Component
public class TikaFileMimeTypeValidator implements FileMimeTypeValidator {

    private static final Map<FileFormat, Set<String>> ALLOWED_MIME_TYPES = Map.of(
            FileFormat.DOCX, Set.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            FileFormat.XLSX, Set.of("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
            FileFormat.PPTX, Set.of("application/vnd.openxmlformats-officedocument.presentationml.presentation"),
            FileFormat.PDF, Set.of("application/pdf")
    );

    private final Tika tika = new Tika();

    @Override
    public void validate(MultipartFile file, FileFormat expectedFormat) {
        if (file == null) {
            throw new InvalidFileTypeException("Arquivo é obrigatório para validar o tipo");
        }

        if (expectedFormat == null) {
            throw new InvalidFileTypeException("Formato esperado é obrigatório para validar o tipo");
        }

        try {
            String detectedMimeType = tika.detect(file.getInputStream());
            Set<String> allowedMimeTypes = ALLOWED_MIME_TYPES.get(expectedFormat);

            if (allowedMimeTypes == null || !allowedMimeTypes.contains(detectedMimeType)) {
                throw new InvalidFileTypeException(
                        "Tipo real do arquivo não corresponde ao formato informado"
                );
            }
        } catch (IOException exception) {
            throw new InvalidFileTypeException("Erro ao validar tipo do arquivo", exception);
        }
    }
}
