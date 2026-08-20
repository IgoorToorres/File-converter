package com.igor.fileconverter.application.usecase;

import com.igor.fileconverter.application.service.FileFormatDetector;
import com.igor.fileconverter.application.service.FileStorageService;
import com.igor.fileconverter.application.service.SupportedConversionPolicy;
import com.igor.fileconverter.domain.entity.Conversion;
import com.igor.fileconverter.domain.enums.ConversionStatus;
import com.igor.fileconverter.domain.enums.FileFormat;
import com.igor.fileconverter.domain.exception.DomainException;
import com.igor.fileconverter.domain.exception.UnsupportedConversionException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateConversionUseCaseTest {

    private final FakeFileStorageService fileStorageService = new FakeFileStorageService();
    private final FileFormatDetector fileFormatDetector = originalFileName -> FileFormat.DOCX;
    private final FakeSupportedConversionPolicy supportedConversionPolicy = new FakeSupportedConversionPolicy();
    private final CreateConversionUseCase useCase = new CreateConversionUseCase(
            fileStorageService,
            fileFormatDetector,
            supportedConversionPolicy
    );

    @Test
    void shouldCreatePendingConversionFromUploadedFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "documento.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "conteudo".getBytes()
        );

        Conversion conversion = useCase.execute(file, FileFormat.PDF);

        assertNotNull(conversion.getId());
        assertEquals(ConversionStatus.PENDING, conversion.getStatus());
        assertEquals("documento.docx", conversion.getOriginalFileName());
        assertEquals("storage-key.docx", conversion.getStoredFileName());
        assertEquals(FileFormat.DOCX, conversion.getSourceFormat());
        assertEquals(FileFormat.PDF, conversion.getTargetFormat());
        assertEquals("storage-key.docx", conversion.getInputStorageKey());
        assertEquals("documento.docx", fileStorageService.originalFileName);
        assertEquals(FileFormat.DOCX, supportedConversionPolicy.sourceFormat);
        assertEquals(FileFormat.PDF, supportedConversionPolicy.targetFormat);
    }

    @Test
    void shouldRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "documento.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                new byte[0]
        );

        assertThrows(DomainException.class, () -> useCase.execute(file, FileFormat.PDF));
    }

    @Test
    void shouldRejectUnsupportedConversionBeforeStoringFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "documento.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "conteudo".getBytes()
        );
        supportedConversionPolicy.reject = true;

        assertThrows(UnsupportedConversionException.class, () -> useCase.execute(file, FileFormat.XLSX));
        assertEquals(0, fileStorageService.storeCalls);
    }

    private static class FakeFileStorageService implements FileStorageService {

        private String originalFileName;
        private int storeCalls;

        @Override
        public String store(String originalFileName, InputStream inputStream) {
            this.originalFileName = originalFileName;
            this.storeCalls++;
            return "storage-key.docx";
        }

        @Override
        public Path load(String storageKey) {
            return Path.of(storageKey);
        }

        @Override
        public void delete(String storageKey) {
        }

        @Override
        public boolean exists(String storageKey) {
            return true;
        }
    }

    private static class FakeSupportedConversionPolicy implements SupportedConversionPolicy {

        private FileFormat sourceFormat;
        private FileFormat targetFormat;
        private boolean reject;

        @Override
        public void validate(FileFormat sourceFormat, FileFormat targetFormat) {
            this.sourceFormat = sourceFormat;
            this.targetFormat = targetFormat;

            if (reject) {
                throw new UnsupportedConversionException("Conversão não suportada");
            }
        }
    }
}
