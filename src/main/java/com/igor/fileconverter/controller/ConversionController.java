package com.igor.fileconverter.controller;

import com.igor.fileconverter.application.usecase.CreateConversionUseCase;
import com.igor.fileconverter.controller.dto.CreateConversionRequest;
import com.igor.fileconverter.controller.dto.CreateConversionResponse;
import com.igor.fileconverter.domain.entity.Conversion;
import com.igor.fileconverter.domain.enums.FileFormat;
import com.igor.fileconverter.shared.error.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/conversions")
@Tag(name = "Conversions", description = "Operacoes de upload e conversao de arquivos")
public class ConversionController {

    private final CreateConversionUseCase createConversionUseCase;

    public ConversionController(CreateConversionUseCase createConversionUseCase){
        this.createConversionUseCase = createConversionUseCase;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar uma conversao",
            description = "Recebe um arquivo, valida o formato inicial, salva no storage local e cria uma conversao pendente.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = CreateConversionRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Conversao criada",
                            content = @Content(schema = @Schema(implementation = CreateConversionResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Arquivo ou formato invalido",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Erro interno inesperado",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                    )
            }
    )
    public CreateConversionResponse create(
            @Parameter(
                    hidden = true
            )
            @RequestPart("file") MultipartFile file,
            @Parameter(
                    hidden = true
            )
            @RequestParam("targetFormat") FileFormat targetFormat
        ){
        Conversion conversion = createConversionUseCase.execute(file, targetFormat);

        return CreateConversionResponse.from(conversion);
    }

}
