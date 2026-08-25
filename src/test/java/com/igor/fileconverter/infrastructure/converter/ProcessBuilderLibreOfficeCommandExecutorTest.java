package com.igor.fileconverter.infrastructure.converter;

import com.igor.fileconverter.config.ConversionProperties;
import com.igor.fileconverter.domain.exception.ConversionProcessingException;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ProcessBuilderLibreOfficeCommandExecutorTest {

    private final ConversionProperties conversionProperties = new ConversionProperties();
    private final ProcessBuilderLibreOfficeCommandExecutor executor =
            new ProcessBuilderLibreOfficeCommandExecutor(conversionProperties);

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

    @Test
    void shouldRejectBlankLibreOfficeCommand() {
        conversionProperties.setLibreOfficeCommand(" ");

        assertThrows(
                ConversionProcessingException.class,
                () -> executor.convertToPdf(Path.of("documento.docx"), Path.of("output"))
        );
    }
}
