package com.github.slamdev.microframework.resources;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;

import static com.github.slamdev.microframework.resources.FilenameUtils.getFilename;

@RequiredArgsConstructor
public class ClassPathResource implements Resource {

    @Getter
    private final String path;

    @Override
    public InputStream getInputStream() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader.getResourceAsStream(path);
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public boolean exists() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader.getResource(path) != null;
    }

    @Override
    public String getName() {
        return getFilename(path);
    }
}
