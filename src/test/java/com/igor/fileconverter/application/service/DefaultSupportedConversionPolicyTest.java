package com.igor.fileconverter.application.service;

import com.igor.fileconverter.domain.enums.FileFormat;
import com.igor.fileconverter.domain.exception.UnsupportedConversionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultSupportedConversionPolicyTest {

    private final DefaultSupportedConversionPolicy policy = new DefaultSupportedConversionPolicy();

    @Test
    void shouldAllowDocxToPdf() {
        assertDoesNotThrow(() -> policy.validate(FileFormat.DOCX, FileFormat.PDF));
    }

    @Test
    void shouldAllowXlsxToPdf() {
        assertDoesNotThrow(() -> policy.validate(FileFormat.XLSX, FileFormat.PDF));
    }

    @Test
    void shouldAllowPptxToPdf() {
        assertDoesNotThrow(() -> policy.validate(FileFormat.PPTX, FileFormat.PDF));
    }

    @Test
    void shouldRejectSameSourceAndTargetFormat() {
        assertThrows(
                UnsupportedConversionException.class,
                () -> policy.validate(FileFormat.PDF, FileFormat.PDF)
        );
    }

    @Test
    void shouldRejectPdfToDocx() {
        assertThrows(
                UnsupportedConversionException.class,
                () -> policy.validate(FileFormat.PDF, FileFormat.DOCX)
        );
    }

    @Test
    void shouldRejectDocxToXlsx() {
        assertThrows(
                UnsupportedConversionException.class,
                () -> policy.validate(FileFormat.DOCX, FileFormat.XLSX)
        );
    }

    @Test
    void shouldRejectNullSourceFormat() {
        assertThrows(
                UnsupportedConversionException.class,
                () -> policy.validate(null, FileFormat.PDF)
        );
    }

    @Test
    void shouldRejectNullTargetFormat() {
        assertThrows(
                UnsupportedConversionException.class,
                () -> policy.validate(FileFormat.DOCX, null)
        );
    }
}
