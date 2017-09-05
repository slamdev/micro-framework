package com.github.slamdev.microframework.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

public class ResourceLoader {

    private static final AntPathMatcher MATCHER = new AntPathMatcher.Builder().build();

    private final Map<String, Function<String, Resource>> resolvers = new HashMap<>();

    public ResourceLoader() {
        this(emptyMap());
    }

    public ResourceLoader(Map<String, Function<String, Resource>> resolvers) {
        this.resolvers.putAll(resolvers);
        this.resolvers.put("classpath", this::getClassPathResource);
    }

    public Resource getClassPathResource(String location) {
        if (location.startsWith("classpath:")) {
            location = location.substring("classpath:".length());
        }
        if (location.startsWith("/")) {
            location = location.substring(1);
        }
        return new ClassPathResource(location);
    }

    public Resource getResource(String location) {
        Function<String, Resource> resolver = resolvers.get(location.split(":")[0]);
        if (resolver == null) {
            throw new IllegalArgumentException("Provider protocol for " + location + " does not have own resolver");
        }
        return resolver.apply(location);
    }

    public List<Resource> getResources(String pattern) {
        if (isPattern(pattern)) {
            return getResourcesFromPattern(pattern);
        }
        return getPlainResources(pattern);
    }

    private List<Resource> getPlainResources(String location) {
        if (location.startsWith("classpath:")) {
            location = location.substring("classpath:".length());
        }
        if (location.startsWith("/")) {
            location = location.substring(1);
        }
        if (location.endsWith("/")) {
            location = location.substring(0, location.length() - 1);
        }
        List<String> filenames = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream in = classLoader.getResourceAsStream(location); BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String resource;
            while ((resource = br.readLine()) != null) {
                filenames.add(String.join("/", location, resource));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        return filenames.stream().map(ClassPathResource::new).collect(toList());
    }

    private List<Resource> getResourcesFromPattern(String pattern) {
        String rootDirPath = determineRootDir(pattern);
        String subPattern = pattern.substring(rootDirPath.length());
        List<Resource> resources = getPlainResources(rootDirPath);
        return resources.stream().filter(r -> MATCHER.isMatch(subPattern, r.getPath())).collect(toList());
    }

    private String determineRootDir(String location) {
        int prefixEnd = location.indexOf(":") + 1;
        int rootDirEnd = location.length();
        while (rootDirEnd > prefixEnd && isPattern(location.substring(prefixEnd, rootDirEnd))) {
            rootDirEnd = location.lastIndexOf('/', rootDirEnd - 2) + 1;
        }
        if (rootDirEnd == 0) {
            rootDirEnd = prefixEnd;
        }
        return location.substring(0, rootDirEnd);
    }

    private boolean isPattern(String path) {
        return (path.indexOf('*') != -1 || path.indexOf('?') != -1);
    }
}
