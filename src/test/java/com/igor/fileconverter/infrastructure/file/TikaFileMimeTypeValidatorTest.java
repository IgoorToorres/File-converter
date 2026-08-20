package com.igor.fileconverter.infrastructure.file;

import com.igor.fileconverter.domain.enums.FileFormat;
import com.igor.fileconverter.domain.exception.InvalidFileTypeException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TikaFileMimeTypeValidatorTest {

    private final TikaFileMimeTypeValidator validator = new TikaFileMimeTypeValidator();

    @Test
    void shouldAcceptPdfWhenContentIsPdf() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "arquivo.pdf",
                "application/pdf",
                "%PDF-1.4\nconteudo fake".getBytes()
        );

        assertDoesNotThrow(() -> validator.validate(file, FileFormat.PDF));
    }

    @Test
    void shouldRejectPdfContentWhenExpectedDocx() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "documento.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "%PDF-1.4\nconteudo fake".getBytes()
        );

        assertThrows(
                InvalidFileTypeException.class,
                () -> validator.validate(file, FileFormat.DOCX)
        );
    }

    @Test
    void shouldRejectTextContentWhenExpectedPdf() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "arquivo.pdf",
                "application/pdf",
                "texto comum".getBytes()
        );

        assertThrows(
                InvalidFileTypeException.class,
                () -> validator.validate(file, FileFormat.PDF)
        );
    }

    @Test
    void shouldRejectNullFile() {
        assertThrows(
                InvalidFileTypeException.class,
                () -> validator.validate(null, FileFormat.PDF)
        );
    }

    @Test
    void shouldRejectNullExpectedFormat() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "arquivo.pdf",
                "application/pdf",
                "%PDF-1.4\nconteudo fake".getBytes()
        );

        assertThrows(
                InvalidFileTypeException.class,
                () -> validator.validate(file, null)
        );
    }
}
