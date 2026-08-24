package com.igor.fileconverter.infrastructure.converter;

import com.igor.fileconverter.domain.converter.ConversionRequest;
import com.igor.fileconverter.domain.converter.ConversionResult;
import com.igor.fileconverter.domain.enums.FileFormat;
import com.igor.fileconverter.domain.exception.ConversionProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocxToPdfConverterTest {

    @TempDir
    Path tempDirectory;

    @Test
    void shouldSupportDocxToPdf() {
        DocxToPdfConverter converter = new DocxToPdfConverter(new FakeLibreOfficeCommandExecutor());

        assertTrue(converter.supports(FileFormat.DOCX, FileFormat.PDF));
    }

    @Test
    void shouldNotSupportXlsxToPdf() {
        DocxToPdfConverter converter = new DocxToPdfConverter(new FakeLibreOfficeCommandExecutor());

        assertFalse(converter.supports(FileFormat.XLSX, FileFormat.PDF));
    }

    @Test
    void shouldNotSupportDocxToXlsx() {
        DocxToPdfConverter converter = new DocxToPdfConverter(new FakeLibreOfficeCommandExecutor());

        assertFalse(converter.supports(FileFormat.DOCX, FileFormat.XLSX));
    }

    @Test
    void shouldConvertDocxToPdf() throws Exception {
        Path inputFile = tempDirectory.resolve("documento.docx");
        Files.writeString(inputFile, "conteudo fake docx");

        FakeLibreOfficeCommandExecutor executor = new FakeLibreOfficeCommandExecutor();
        DocxToPdfConverter converter = new DocxToPdfConverter(executor);
        ConversionRequest request = new ConversionRequest(
                inputFile,
                tempDirectory,
                FileFormat.DOCX,
                FileFormat.PDF
        );

        ConversionResult result = converter.convert(request);

        assertEquals(tempDirectory.resolve("documento.pdf"), result.outputFile());
        assertTrue(Files.exists(result.outputFile()));
        assertEquals(inputFile, executor.inputFile);
        assertEquals(tempDirectory, executor.outputDirectory);
    }

    @Test
    void shouldRejectNullRequest() {
        DocxToPdfConverter converter = new DocxToPdfConverter(new FakeLibreOfficeCommandExecutor());

        assertThrows(ConversionProcessingException.class, () -> converter.convert(null));
    }

    @Test
    void shouldRejectUnsupportedFormats() {
        DocxToPdfConverter converter = new DocxToPdfConverter(new FakeLibreOfficeCommandExecutor());
        ConversionRequest request = new ConversionRequest(
                tempDirectory.resolve("planilha.xlsx"),
                tempDirectory,
                FileFormat.XLSX,
                FileFormat.PDF
        );

        assertThrows(ConversionProcessingException.class, () -> converter.convert(request));
    }

    @Test
    void shouldThrowExceptionWhenOutputFileIsNotGenerated() throws Exception {
        Path inputFile = tempDirectory.resolve("documento.docx");
        Files.writeString(inputFile, "conteudo fake docx");

        DocxToPdfConverter converter = new DocxToPdfConverter(new FakeLibreOfficeCommandExecutor(false));
        ConversionRequest request = new ConversionRequest(
                inputFile,
                tempDirectory,
                FileFormat.DOCX,
                FileFormat.PDF
        );

        assertThrows(ConversionProcessingException.class, () -> converter.convert(request));
    }

    @Test
    void shouldPropagateExecutorFailure() throws Exception {
        Path inputFile = tempDirectory.resolve("documento.docx");
        Files.writeString(inputFile, "conteudo fake docx");

        FakeLibreOfficeCommandExecutor executor = new FakeLibreOfficeCommandExecutor();
        executor.reject = true;

        DocxToPdfConverter converter = new DocxToPdfConverter(executor);
        ConversionRequest request = new ConversionRequest(
                inputFile,
                tempDirectory,
                FileFormat.DOCX,
                FileFormat.PDF
        );

        assertThrows(ConversionProcessingException.class, () -> converter.convert(request));
    }

    private static class FakeLibreOfficeCommandExecutor implements LibreOfficeCommandExecutor {

        private final boolean shouldGenerateOutput;
        private Path inputFile;
        private Path outputDirectory;
        private boolean reject;

        private FakeLibreOfficeCommandExecutor() {
            this(true);
        }

        private FakeLibreOfficeCommandExecutor(boolean shouldGenerateOutput) {
            this.shouldGenerateOutput = shouldGenerateOutput;
        }

        @Override
        public void convertToPdf(Path inputFile, Path outputDirectory) {
            this.inputFile = inputFile;
            this.outputDirectory = outputDirectory;

            if (reject) {
                throw new ConversionProcessingException("LibreOffice falhou ao converter arquivo");
            }

            if (!shouldGenerateOutput) {
                return;
            }

            try {
                String inputFileName = inputFile.getFileName().toString();
                String outputFileName = inputFileName.substring(0, inputFileName.lastIndexOf('.')) + ".pdf";
                Files.writeString(outputDirectory.resolve(outputFileName), "pdf fake");
            } catch (Exception exception) {
                throw new ConversionProcessingException("Erro ao gerar PDF fake", exception);
            }
        }
    }
}
