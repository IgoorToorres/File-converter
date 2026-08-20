package com.igor.fileconverter.application.service;

import com.igor.fileconverter.domain.enums.FileFormat;

public interface FileFormatDetector {
    FileFormat detect(String originalFileName);
}
