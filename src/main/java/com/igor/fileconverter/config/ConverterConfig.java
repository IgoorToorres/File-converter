package com.igor.fileconverter.config;


import com.igor.fileconverter.domain.converter.ConverterRegistry;
import com.igor.fileconverter.domain.converter.FileConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ConverterConfig {

    @Bean
    public ConverterRegistry converterRegistry(List<FileConverter> converters) {
        return new ConverterRegistry(converters);
    }
}
