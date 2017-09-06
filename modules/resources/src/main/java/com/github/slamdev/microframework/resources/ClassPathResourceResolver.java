package com.github.slamdev.microframework.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static com.github.slamdev.microframework.resources.AntPathMatcher.Builder;
import static com.github.slamdev.microframework.resources.AntPathMatcher.isPattern;
import static com.github.slamdev.microframework.resources.FilenameUtils.determineRootDir;
import static java.util.stream.Collectors.toList;

public class ClassPathResourceResolver implements ResourceResolver<ClassPathResource> {

    private static final String PROTOCOL = "classpath:";

    private static final AntPathMatcher MATCHER = new Builder().build();

    @Override
    public ClassPathResource getResource(String location) {
        return new ClassPathResource(stripProtocol(location));
    }

    @Override
    public List<ClassPathResource> getResources(String pattern) {
        if (isPattern(pattern)) {
            return getResourcesFromPattern(pattern);
        }
        return getPlainResources(pattern);
    }

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    private List<ClassPathResource> getResourcesFromPattern(String pattern) {
        String rootDirPath = determineRootDir(pattern);
        String subPattern = pattern.substring(rootDirPath.length());
        List<ClassPathResource> resources = getPlainResources(rootDirPath);
        return resources.stream().filter(r -> MATCHER.isMatch(subPattern, r.getPath())).collect(toList());
    }

    private List<ClassPathResource> getPlainResources(String location) {
        location = stripProtocol(location);
        if (location.endsWith("/")) {
            location = location.substring(0, location.length() - 1);
        }
        List<String> filenames = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream in = classLoader.getResourceAsStream(location);
             BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String resource;
            while ((resource = br.readLine()) != null) {
                filenames.add(String.join("/", location, resource));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        return filenames.stream().map(ClassPathResource::new).collect(toList());
    }

    private String stripProtocol(String location) {
        if (location.startsWith(PROTOCOL)) {
            location = location.substring((PROTOCOL).length());
        }
        if (location.startsWith("/")) {
            location = location.substring(1);
        }
        return location;
    }
}
