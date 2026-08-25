package com.igor.fileconverter.controller.dto;

import com.igor.fileconverter.domain.entity.Conversion;
import com.igor.fileconverter.domain.enums.ConversionStatus;
import com.igor.fileconverter.domain.enums.FileFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Dados retornados apos a criacao de uma conversao")
public record CreateConversionResponse(
        @Schema(description = "Identificador da conversao")
        UUID id,
        @Schema(description = "Status atual da conversao", example = "PENDING")
        ConversionStatus status,
        @Schema(description = "Nome original enviado pelo usuario", example = "documento.docx")
        String originalFileName,
        @Schema(description = "Formato detectado a partir do arquivo", example = "DOCX")
        FileFormat sourceFormat,
        @Schema(description = "Formato solicitado para a conversao", example = "PDF")
        FileFormat targetFormat,
        @Schema(description = "Chave do arquivo salvo no storage local", example = "4d87b32a-6ef5-4d80-88b4-935883a4a27f.docx")
        String inputStorageKey,
        @Schema(description = "Chave do arquivo convertido salvo no storage local", example = "0df45e38-8e93-4f6f-b518-6ad4fc8b9f34.pdf")
        String outputStorageKey
) {
    public static CreateConversionResponse from(Conversion conversion) {
        return new CreateConversionResponse(
                conversion.getId(),
                conversion.getStatus(),
                conversion.getOriginalFileName(),
                conversion.getSourceFormat(),
                conversion.getTargetFormat(),
                conversion.getInputStorageKey(),
                conversion.getOutputStorageKey()
        );
    }
}
