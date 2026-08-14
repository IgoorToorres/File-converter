package com.igor.fileconverter.domain.converter;

import com.igor.fileconverter.domain.enums.FileFormat;

public interface FileConverter {
    boolean supports(FileFormat sourceFormat, FileFormat targetFormat);
    ConversionResult convert(ConversionRequest request);
}
