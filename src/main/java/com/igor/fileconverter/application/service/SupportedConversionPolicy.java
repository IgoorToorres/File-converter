package com.igor.fileconverter.application.service;

import com.igor.fileconverter.domain.enums.FileFormat;

public interface SupportedConversionPolicy {

    void validate(FileFormat sourceFormat, FileFormat targetFormat);
}
