package com.igor.fileconverter.application.usecase;

import com.igor.fileconverter.application.service.FileFormatDetector;
import com.igor.fileconverter.application.service.FileMimeTypeValidator;
import com.igor.fileconverter.application.service.FileStorageService;
import com.igor.fileconverter.application.service.SupportedConversionPolicy;
import com.igor.fileconverter.domain.converter.ConversionRequest;
import com.igor.fileconverter.domain.converter.ConversionResult;
import com.igor.fileconverter.domain.converter.ConverterRegistry;
import com.igor.fileconverter.domain.converter.FileConverter;
import com.igor.fileconverter.domain.entity.Conversion;
import com.igor.fileconverter.domain.enums.ConversionStatus;
import com.igor.fileconverter.domain.enums.FileFormat;
import com.igor.fileconverter.domain.exception.ConversionProcessingException;
import com.igor.fileconverter.domain.exception.DomainException;
import com.igor.fileconverter.domain.exception.InvalidFileTypeException;
import com.igor.fileconverter.domain.exception.UnsupportedConversionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateConversionUseCaseTest {

    @TempDir
    Path tempDirectory;

    private final FakeFileStorageService fileStorageService = new FakeFileStorageService();
    private final FileFormatDetector fileFormatDetector = originalFileName -> FileFormat.DOCX;
    private final FakeFileMimeTypeValidator fileMimeTypeValidator = new FakeFileMimeTypeValidator();
    private final FakeSupportedConversionPolicy supportedConversionPolicy = new FakeSupportedConversionPolicy();
    private final FakeFileConverter fileConverter = new FakeFileConverter();
    private final ConverterRegistry converterRegistry = new ConverterRegistry(List.of(fileConverter));
    private final CreateConversionUseCase useCase = new CreateConversionUseCase(
            fileStorageService,
            fileFormatDetector,
            fileMimeTypeValidator,
            supportedConversionPolicy,
            converterRegistry
    );

    @Test
    void shouldCreateCompletedConversionFromUploadedFile() {
        fileStorageService.inputFile = tempDirectory.resolve("input-storage-key.docx");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "documento.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "conteudo".getBytes()
        );

        Conversion conversion = useCase.execute(file, FileFormat.PDF);

        assertNotNull(conversion.getId());
        assertEquals(ConversionStatus.COMPLETED, conversion.getStatus());
        assertEquals("documento.docx", conversion.getOriginalFileName());
        assertEquals("input-storage-key.docx", conversion.getStoredFileName());
        assertEquals(FileFormat.DOCX, conversion.getSourceFormat());
        assertEquals(FileFormat.PDF, conversion.getTargetFormat());
        assertEquals("input-storage-key.docx", conversion.getInputStorageKey());
        assertEquals("output-storage-key.pdf", conversion.getOutputStorageKey());
        assertEquals("documento.docx", fileStorageService.originalFileName);
        assertEquals("documento.pdf", fileStorageService.outputFileName);
        assertEquals(FileFormat.DOCX, fileMimeTypeValidator.expectedFormat);
        assertEquals(FileFormat.DOCX, supportedConversionPolicy.sourceFormat);
        assertEquals(FileFormat.PDF, supportedConversionPolicy.targetFormat);
        assertEquals(fileStorageService.inputFile, fileConverter.inputFile);
        assertEquals(FileFormat.DOCX, fileConverter.sourceFormat);
        assertEquals(FileFormat.PDF, fileConverter.targetFormat);
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

    @Test
    void shouldRejectInvalidMimeTypeBeforeValidatingPolicyAndStoringFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "documento.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "conteudo".getBytes()
        );
        fileMimeTypeValidator.reject = true;

        assertThrows(InvalidFileTypeException.class, () -> useCase.execute(file, FileFormat.PDF));
        assertEquals(0, supportedConversionPolicy.validateCalls);
        assertEquals(0, fileStorageService.storeCalls);
    }

    @Test
    void shouldFailWhenConverterFails() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "documento.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "conteudo".getBytes()
        );
        fileConverter.reject = true;

        assertThrows(ConversionProcessingException.class, () -> useCase.execute(file, FileFormat.PDF));
        assertEquals(1, fileStorageService.storeCalls);
        assertEquals(0, fileStorageService.storePathCalls);
    }

    private static class FakeFileStorageService implements FileStorageService {

        private String originalFileName;
        private String outputFileName;
        private int storeCalls;
        private int storePathCalls;
        private Path inputFile;

        @Override
        public String store(String originalFileName, InputStream inputStream) {
            this.originalFileName = originalFileName;
            this.storeCalls++;
            return "input-storage-key.docx";
        }

        @Override
        public String store(String originalFileName, Path sourceFile) {
            this.outputFileName = originalFileName;
            this.storePathCalls++;
            return "output-storage-key.pdf";
        }

        @Override
        public Path load(String storageKey) {
            if (inputFile == null) {
                inputFile = Path.of(storageKey);
            }

            return inputFile;
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
        private int validateCalls;

        @Override
        public void validate(FileFormat sourceFormat, FileFormat targetFormat) {
            this.validateCalls++;
            this.sourceFormat = sourceFormat;
            this.targetFormat = targetFormat;

            if (reject) {
                throw new UnsupportedConversionException("Conversão não suportada");
            }
        }
    }

    private static class FakeFileMimeTypeValidator implements FileMimeTypeValidator {

        private FileFormat expectedFormat;
        private boolean reject;

        @Override
        public void validate(MultipartFile file, FileFormat expectedFormat) {
            this.expectedFormat = expectedFormat;

            if (reject) {
                throw new InvalidFileTypeException("Tipo real do arquivo inválido");
            }
        }
    }

    private static class FakeFileConverter implements FileConverter {

        private Path inputFile;
        private FileFormat sourceFormat;
        private FileFormat targetFormat;
        private boolean reject;

        @Override
        public boolean supports(FileFormat sourceFormat, FileFormat targetFormat) {
            return sourceFormat == FileFormat.DOCX && targetFormat == FileFormat.PDF;
        }

        @Override
        public ConversionResult convert(ConversionRequest request) {
            this.inputFile = request.inputFile();
            this.sourceFormat = request.sourceFormat();
            this.targetFormat = request.targetFormat();

            if (reject) {
                throw new ConversionProcessingException("Erro ao converter arquivo");
            }

            try {
                Path outputFile = request.outputDirectory().resolve("documento.pdf");
                Files.writeString(outputFile, "pdf fake");
                return new ConversionResult(outputFile);
            } catch (IOException exception) {
                throw new ConversionProcessingException("Erro ao gerar PDF fake", exception);
            }
        }
    }
}
