package com.igor.fileconverter.infrastructure.converter;

import com.igor.fileconverter.domain.exception.ConversionProcessingException;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ProcessBuilderLibreOfficeCommandExecutorTest {

    private final ProcessBuilderLibreOfficeCommandExecutor executor =
            new ProcessBuilderLibreOfficeCommandExecutor();

    @Test
    void shouldRejectNullInputFile() {
        assertThrows(
                ConversionProcessingException.class,
                () -> executor.convertToPdf(null, Path.of("output"))
        );
    }

    @Test
    void shouldRejectNullOutputDirectory() {
        assertThrows(
                ConversionProcessingException.class,
                () -> executor.convertToPdf(Path.of("documento.docx"), null)
        );
    }
}
