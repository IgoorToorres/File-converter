package com.igor.fileconverter.controller.dto;

import com.igor.fileconverter.domain.enums.FileFormat;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Formulario multipart para criar uma conversao")
public record CreateConversionRequest(
        @Schema(
                description = "Arquivo original",
                type = "string",
                format = "binary",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String file,
        @Schema(
                description = "Formato de destino",
                example = "PDF",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        FileFormat targetFormat
) {
}
