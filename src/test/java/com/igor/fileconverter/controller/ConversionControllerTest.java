package com.igor.fileconverter.controller;

import com.igor.fileconverter.application.usecase.CreateConversionUseCase;
import com.igor.fileconverter.domain.entity.Conversion;
import com.igor.fileconverter.domain.enums.FileFormat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConversionController.class)
@Import(ConversionControllerTest.TestConfig.class)
class ConversionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateConversion() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "documento.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "conteudo".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/conversions")
                        .file(file)
                        .param("targetFormat", "PDF"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.originalFileName").value("documento.docx"))
                .andExpect(jsonPath("$.sourceFormat").value("DOCX"))
                .andExpect(jsonPath("$.targetFormat").value("PDF"))
                .andExpect(jsonPath("$.inputStorageKey").value("storage-key.docx"));
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        CreateConversionUseCase createConversionUseCase() {
            return new CreateConversionUseCase(null, null, null, null) {
                @Override
                public Conversion execute(MultipartFile file, FileFormat targetFormat) {
                    return Conversion.create(
                            "documento.docx",
                            "storage-key.docx",
                            FileFormat.DOCX,
                            targetFormat,
                            "storage-key.docx"
                    );
                }
            };
        }
    }
}
