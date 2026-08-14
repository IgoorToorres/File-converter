package com.igor.fileconverter.application.service;

import java.io.InputStream;
import java.nio.file.Path;

public interface FileStorageService {
    String store(String originalFileName, InputStream inputStream);

    Path load(String storageKey);

    void delete(String storageKey);

    boolean exists(String storageKey);
}
