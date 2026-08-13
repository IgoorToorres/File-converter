package com.igor.fileconverter.domain.entity;

import com.igor.fileconverter.domain.enums.ConversionStatus;
import com.igor.fileconverter.domain.enums.FileFormat;
import com.igor.fileconverter.domain.exception.DomainException;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Conversion {

    private static final Duration DEFAULT_EXPIRATION_TIME = Duration.ofHours(24);

    private UUID id;

    private String originalFileName;

    private String storedFileName;

    private FileFormat sourceFormat;

    private FileFormat targetFormat;

    private ConversionStatus status;

    private Instant createdAt;

    private Instant startedAt;

    private Instant completedAt;

    private Instant updatedAt;

    private Instant expiresAt;

    private String inputStorageKey;

    private String outputStorageKey;

    private String errorMessage;

    public Conversion(
            String originalFileName,
            String storedFileName,
            FileFormat sourceFormat,
            FileFormat targetFormat,
            String inputStorageKey
    ) {
        validateRequiredText(originalFileName, "Nome original do arquivo é obrigatório");
        validateRequiredText(storedFileName, "Nome armazenado do arquivo é obrigatório");
        validateRequiredText(inputStorageKey, "Chave de armazenamento do arquivo original é obrigatória");

        Objects.requireNonNull(sourceFormat, "Formato de origem é obrigatório");
        Objects.requireNonNull(targetFormat, "Formato de destino é obrigatório");

        if (sourceFormat == targetFormat) {
            throw new DomainException("Formato de origem e destino não podem ser iguais");
        }

        this.id = UUID.randomUUID();
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.sourceFormat = sourceFormat;
        this.targetFormat = targetFormat;
        this.status = ConversionStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.expiresAt = this.createdAt.plus(DEFAULT_EXPIRATION_TIME);
        this.startedAt = null;
        this.completedAt = null;
        this.inputStorageKey = inputStorageKey;
        this.outputStorageKey = null;
        this.errorMessage = null;
    }

    public static Conversion create(
            String originalFileName,
            String storedFileName,
            FileFormat sourceFormat,
            FileFormat targetFormat,
            String inputStorageKey
    ) {
        return new Conversion(originalFileName, storedFileName, sourceFormat, targetFormat, inputStorageKey);
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public void startProcessing() {
        if (!canStartProcessing()) {
            throw new DomainException("Status precisa estar pendente para começar o processo");
        }

        this.status = ConversionStatus.PROCESSING;
        this.startedAt = Instant.now();
        touch();
    }

    public void complete(String outputStorageKey) {
        validateRequiredText(outputStorageKey, "Chave de armazenamento do arquivo convertido é obrigatória");

        if (!canComplete()) {
            throw new DomainException("Status precisa estar processando para completar");
        }

        this.status = ConversionStatus.COMPLETED;
        this.outputStorageKey = outputStorageKey;
        this.completedAt = Instant.now();
        touch();
    }

    public void fail(String errorMessage) {
        validateRequiredText(errorMessage, "Mensagem de erro é obrigatória");

        if (!canFail()) {
            throw new DomainException("Conversão só pode falhar se estiver pendente ou processando");
        }

        this.status = ConversionStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = Instant.now();
        touch();
    }

    public void expire() {
        if (!canExpire()) {
            throw new DomainException("Conversão só pode expirar depois de completada");
        }

        this.status = ConversionStatus.EXPIRED;
        touch();
    }

    public boolean isPending() {
        return this.status == ConversionStatus.PENDING;
    }

    public boolean isProcessing() {
        return this.status == ConversionStatus.PROCESSING;
    }

    public boolean isCompleted() {
        return this.status == ConversionStatus.COMPLETED;
    }

    public boolean isFailed() {
        return this.status == ConversionStatus.FAILED;
    }

    public boolean isExpired() {
        return this.status == ConversionStatus.EXPIRED;
    }

    public boolean canStartProcessing() {
        return this.status == ConversionStatus.PENDING;
    }

    public boolean canComplete() {
        return this.status == ConversionStatus.PROCESSING;
    }

    public boolean canFail() {
        return this.status == ConversionStatus.PENDING || this.status == ConversionStatus.PROCESSING;
    }

    public boolean canExpire() {
        return this.status == ConversionStatus.COMPLETED;
    }

    public UUID getId() {
        return id;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public FileFormat getSourceFormat() {
        return sourceFormat;
    }

    public FileFormat getTargetFormat() {
        return targetFormat;
    }

    public ConversionStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public String getInputStorageKey() {
        return inputStorageKey;
    }

    public String getOutputStorageKey() {
        return outputStorageKey;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    private static void validateRequiredText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new DomainException(message);
        }
    }
}
