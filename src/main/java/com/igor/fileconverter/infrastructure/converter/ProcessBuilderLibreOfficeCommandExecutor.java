package com.igor.fileconverter.infrastructure.converter;

import com.igor.fileconverter.domain.exception.ConversionProcessingException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class ProcessBuilderLibreOfficeCommandExecutor implements LibreOfficeCommandExecutor {

    private static final Duration TIMEOUT = Duration.ofSeconds(60);

    @Override
    public void convertToPdf(Path inputFile, Path outputDirectory) {
        validate(inputFile, outputDirectory);

        ProcessBuilder processBuilder = new ProcessBuilder(command(inputFile, outputDirectory));
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();

            boolean finished = process.waitFor(TIMEOUT.toSeconds(), TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                throw new ConversionProcessingException("Tempo limite excedido ao converter arquivo");
            }

            if (process.exitValue() != 0) {
                throw new ConversionProcessingException("LibreOffice falhou ao converter arquivo");
            }
        } catch (IOException exception) {
            throw new ConversionProcessingException("Erro ao executar LibreOffice", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ConversionProcessingException("Conversão interrompida", exception);
        }
    }

    private void validate(Path inputFile, Path outputDirectory) {
        if (inputFile == null) {
            throw new ConversionProcessingException("Arquivo de entrada é obrigatório");
        }

        if (outputDirectory == null) {
            throw new ConversionProcessingException("Diretório de saída é obrigatório");
        }
    }

    private List<String> command(Path inputFile, Path outputDirectory) {
        return List.of(
                "libreoffice",
                "--headless",
                "--convert-to",
                "pdf",
                "--outdir",
                outputDirectory.toString(),
                inputFile.toString()
        );
    }
}
