package com.github.slamdev.microframework.resources;

import com.google.common.reflect.ClassPath;
import lombok.SneakyThrows;

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

    @SneakyThrows
    private List<ClassPathResource> getPlainResources(String location) {
        location = stripProtocol(location);
        if (location.endsWith("/")) {
            location = location.substring(0, location.length() - 1);
        }
        String path = location;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return ClassPath.from(classLoader).getResources()
                .stream()
                .map(ClassPath.ResourceInfo::getResourceName)
                .filter(name -> name.startsWith(path))
                .map(ClassPathResource::new).collect(toList());
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
