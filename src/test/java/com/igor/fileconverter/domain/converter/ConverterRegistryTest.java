package com.igor.fileconverter.domain.converter;

import com.igor.fileconverter.domain.enums.FileFormat;
import com.igor.fileconverter.domain.exception.UnsupportedConversionException;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConverterRegistryTest {

    @Test
    void shouldReturnConverterWhenConversionIsSupported() {
        FileConverter docxToPdfConverter = new FakeConverter(FileFormat.DOCX, FileFormat.PDF);
        ConverterRegistry registry = new ConverterRegistry(List.of(docxToPdfConverter));

        FileConverter result = registry.findConverter(FileFormat.DOCX, FileFormat.PDF);

        assertSame(docxToPdfConverter, result);
    }

    @Test
    void shouldThrowExceptionWhenConversionIsNotSupported() {
        FileConverter docxToPdfConverter = new FakeConverter(FileFormat.DOCX, FileFormat.PDF);
        ConverterRegistry registry = new ConverterRegistry(List.of(docxToPdfConverter));

        assertThrows(
                UnsupportedConversionException.class,
                () -> registry.findConverter(FileFormat.XLSX, FileFormat.PDF)
        );
    }

    @Test
    void shouldNotReturnWrongConverter() {
        FileConverter docxToPdfConverter = new FakeConverter(FileFormat.DOCX, FileFormat.PDF);
        FileConverter xlsxToPdfConverter = new FakeConverter(FileFormat.XLSX, FileFormat.PDF);
        ConverterRegistry registry = new ConverterRegistry(List.of(docxToPdfConverter, xlsxToPdfConverter));

        FileConverter result = registry.findConverter(FileFormat.XLSX, FileFormat.PDF);

        assertSame(xlsxToPdfConverter, result);
    }

    private record FakeConverter(
            FileFormat supportedSourceFormat,
            FileFormat supportedTargetFormat
    ) implements FileConverter {

        @Override
        public boolean supports(FileFormat sourceFormat, FileFormat targetFormat) {
            return supportedSourceFormat == sourceFormat && supportedTargetFormat == targetFormat;
        }

        @Override
        public ConversionResult convert(ConversionRequest request) {
            return new ConversionResult(Path.of("output.pdf"));
        }
    }
}
