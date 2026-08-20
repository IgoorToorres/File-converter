package com.igor.fileconverter.application.service;

import com.igor.fileconverter.domain.enums.FileFormat;
import org.springframework.web.multipart.MultipartFile;

public interface FileMimeTypeValidator {

    void validate(MultipartFile file, FileFormat expectedFormat);
}
