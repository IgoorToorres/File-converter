package com.igor.fileconverter.shared.error;

import com.igor.fileconverter.domain.exception.ConversionProcessingException;
import com.igor.fileconverter.domain.exception.InvalidFileTypeException;
import com.igor.fileconverter.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GlobalExceptionHandlerTest.TestController.class)
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerTest.TestController.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnNotFoundWhenResourceDoesNotExist() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.path").value("/test/not-found"));
    }

    @Test
    void shouldReturnInternalServerErrorWhenUnexpectedExceptionOccurs() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Erro interno inesperado"))
                .andExpect(jsonPath("$.path").value("/test/unexpected"));
    }

    @Test
    void shouldReturnPayloadTooLargeWhenUploadExceedsLimit() throws Exception {
        mockMvc.perform(get("/test/file-too-large"))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.status").value(413))
                .andExpect(jsonPath("$.error").value("FILE_TOO_LARGE"))
                .andExpect(jsonPath("$.message").value("Arquivo excede o tamanho máximo permitido"))
                .andExpect(jsonPath("$.path").value("/test/file-too-large"));
    }

    @Test
    void shouldReturnBadRequestWhenFileTypeIsInvalid() throws Exception {
        mockMvc.perform(get("/test/invalid-file-type"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("INVALID_FILE_TYPE"))
                .andExpect(jsonPath("$.message").value("Tipo real do arquivo não corresponde ao formato informado"))
                .andExpect(jsonPath("$.path").value("/test/invalid-file-type"));
    }

    @Test
    void shouldReturnInternalServerErrorWhenConversionProcessingFails() throws Exception {
        mockMvc.perform(get("/test/conversion-processing-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("CONVERSION_PROCESSING_ERROR"))
                .andExpect(jsonPath("$.message").value("LibreOffice falhou ao converter arquivo"))
                .andExpect(jsonPath("$.path").value("/test/conversion-processing-error"));
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/not-found")
        void notFound() {
            throw new ResourceNotFoundException("Recurso não encontrado");
        }

        @GetMapping("/unexpected")
        void unexpected() {
            throw new RuntimeException("Erro técnico");
        }

        @GetMapping("/file-too-large")
        void fileTooLarge() {
            throw new MaxUploadSizeExceededException(10);
        }

        @GetMapping("/invalid-file-type")
        void invalidFileType() {
            throw new InvalidFileTypeException("Tipo real do arquivo não corresponde ao formato informado");
        }

        @GetMapping("/conversion-processing-error")
        void conversionProcessingError() {
            throw new ConversionProcessingException("LibreOffice falhou ao converter arquivo");
        }
    }
}
