/*
 * Copyright (c) 2023 Nextiva, Inc. to Present.
 * All rights reserved.
 */

package com.common.employee.dto;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Common fields for unit testing.
 * Created by marcos.luna on 14/11/23
 */
public class Commons {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.registerModule(new Jdk8Module());
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * Reads file content from resources.
     * @param fileName filename.
     * @return Optional with file content if exists.
     */
    public static Optional<String> readFile(String fileName) {

        String fileContent = null;
        try {
            File imageFile = Paths.get(ClassLoader.getSystemResource(fileName).toURI()).toFile();
            fileContent = Files.readString(Path.of(imageFile.getPath()));
        } catch (IOException | URISyntaxException e) {
            return Optional.empty();
        }
        return Optional.of(fileContent);
    }
}
