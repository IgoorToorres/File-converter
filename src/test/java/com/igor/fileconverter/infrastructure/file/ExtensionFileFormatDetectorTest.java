package com.igor.fileconverter.infrastructure.file;

import com.igor.fileconverter.domain.enums.FileFormat;
import com.igor.fileconverter.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExtensionFileFormatDetectorTest {

    private final ExtensionFileFormatDetector detector = new ExtensionFileFormatDetector();

    @Test
    void shouldDetectDocxFormat() {
        assertEquals(FileFormat.DOCX, detector.detect("documento.docx"));
    }

    @Test
    void shouldDetectXlsxFormat() {
        assertEquals(FileFormat.XLSX, detector.detect("planilha.xlsx"));
    }

    @Test
    void shouldDetectPptxFormat() {
        assertEquals(FileFormat.PPTX, detector.detect("apresentacao.pptx"));
    }

    @Test
    void shouldDetectPdfFormat() {
        assertEquals(FileFormat.PDF, detector.detect("arquivo.pdf"));
    }

    @Test
    void shouldDetectUppercaseExtension() {
        assertEquals(FileFormat.DOCX, detector.detect("documento.DOCX"));
    }

    @Test
    void shouldRejectUnsupportedExtension() {
        assertThrows(DomainException.class, () -> detector.detect("documento.txt"));
    }

    @Test
    void shouldRejectFileWithoutExtension() {
        assertThrows(DomainException.class, () -> detector.detect("documento"));
    }

    @Test
    void shouldRejectBlankFileName() {
        assertThrows(DomainException.class, () -> detector.detect(" "));
    }

    @Test
    void shouldRejectNullFileName() {
        assertThrows(DomainException.class, () -> detector.detect(null));
    }
}
