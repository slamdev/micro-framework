package com.github.slamdev.microframework.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public class ResourceLoader {

    private final List<ResourceResolver> resolvers = new ArrayList<>();

    public ResourceLoader() {
        this(emptyList());
    }

    public ResourceLoader(ResourceResolver... resolvers) {
        this(asList(resolvers));
    }

    public ResourceLoader(List<ResourceResolver> resolvers) {
        this.resolvers.addAll(resolvers);
        this.resolvers.add(new ClassPathResourceResolver());
    }

    public Resource getResource(String location) {
        return resolver(location).orElseThrow(IllegalArgumentException::new).getResource(location);
    }

    @SuppressWarnings("unchecked")
    public List<Resource> getResources(String pattern) {
        return resolver(pattern).orElseThrow(IllegalArgumentException::new).getResources(pattern);
    }

    private Optional<ResourceResolver> resolver(String location) {
        String protocol = location.split(":")[0] + ":";
        return resolvers.stream().filter(r -> r.getProtocol().startsWith(protocol)).findAny();
    }
}
