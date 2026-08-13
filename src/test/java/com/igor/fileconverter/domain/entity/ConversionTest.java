package com.igor.fileconverter.domain.entity;

import com.igor.fileconverter.domain.enums.ConversionStatus;
import com.igor.fileconverter.domain.enums.FileFormat;
import com.igor.fileconverter.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConversionTest {

    @Test
    void shouldCreatePendingConversion() {
        Conversion conversion = createConversion();

        assertNotNull(conversion.getId());
        assertEquals("documento.docx", conversion.getOriginalFileName());
        assertEquals("input-file.docx", conversion.getStoredFileName());
        assertEquals(FileFormat.DOCX, conversion.getSourceFormat());
        assertEquals(FileFormat.PDF, conversion.getTargetFormat());
        assertEquals(ConversionStatus.PENDING, conversion.getStatus());
        assertNotNull(conversion.getCreatedAt());
        assertNotNull(conversion.getUpdatedAt());
        assertNotNull(conversion.getExpiresAt());
        assertEquals("input/uuid/documento.docx", conversion.getInputStorageKey());
        assertNull(conversion.getStartedAt());
        assertNull(conversion.getCompletedAt());
        assertNull(conversion.getOutputStorageKey());
        assertNull(conversion.getErrorMessage());
        assertTrue(conversion.isPending());
        assertTrue(conversion.canStartProcessing());
    }

    @Test
    void shouldNotCreateConversionWithSameSourceAndTargetFormat() {
        assertThrows(DomainException.class, () -> Conversion.create(
                "documento.pdf",
                "input-file.pdf",
                FileFormat.PDF,
                FileFormat.PDF,
                "input/uuid/documento.pdf"
        ));
    }

    @Test
    void shouldStartProcessingPendingConversion() {
        Conversion conversion = createConversion();

        conversion.startProcessing();

        assertEquals(ConversionStatus.PROCESSING, conversion.getStatus());
        assertNotNull(conversion.getStartedAt());
        assertTrue(conversion.isProcessing());
        assertTrue(conversion.canComplete());
        assertTrue(conversion.canFail());
        assertFalse(conversion.canStartProcessing());
    }

    @Test
    void shouldCompleteProcessingConversion() {
        Conversion conversion = createConversion();

        conversion.startProcessing();
        conversion.complete("output/uuid/documento.pdf");

        assertEquals(ConversionStatus.COMPLETED, conversion.getStatus());
        assertEquals("output/uuid/documento.pdf", conversion.getOutputStorageKey());
        assertNotNull(conversion.getCompletedAt());
        assertTrue(conversion.isCompleted());
        assertTrue(conversion.canExpire());
        assertFalse(conversion.canFail());
    }

    @Test
    void shouldNotCompletePendingConversion() {
        Conversion conversion = createConversion();

        assertThrows(DomainException.class, () -> conversion.complete("output/uuid/documento.pdf"));
    }

    @Test
    void shouldFailPendingConversion() {
        Conversion conversion = createConversion();

        conversion.fail("Formato de arquivo inválido");

        assertEquals(ConversionStatus.FAILED, conversion.getStatus());
        assertEquals("Formato de arquivo inválido", conversion.getErrorMessage());
        assertNotNull(conversion.getCompletedAt());
        assertTrue(conversion.isFailed());
    }

    @Test
    void shouldNotFailCompletedConversion() {
        Conversion conversion = createConversion();

        conversion.startProcessing();
        conversion.complete("output/uuid/documento.pdf");

        assertThrows(DomainException.class, () -> conversion.fail("Erro posterior"));
    }

    @Test
    void shouldExpireCompletedConversion() {
        Conversion conversion = createConversion();

        conversion.startProcessing();
        conversion.complete("output/uuid/documento.pdf");
        conversion.expire();

        assertEquals(ConversionStatus.EXPIRED, conversion.getStatus());
        assertTrue(conversion.isExpired());
    }

    @Test
    void shouldNotExpirePendingConversion() {
        Conversion conversion = createConversion();

        assertThrows(DomainException.class, conversion::expire);
    }

    private Conversion createConversion() {
        return Conversion.create(
                "documento.docx",
                "input-file.docx",
                FileFormat.DOCX,
                FileFormat.PDF,
                "input/uuid/documento.docx"
        );
    }
}
