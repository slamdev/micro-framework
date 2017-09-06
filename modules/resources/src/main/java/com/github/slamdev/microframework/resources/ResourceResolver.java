package com.github.slamdev.microframework.resources;

import java.util.List;

public interface ResourceResolver<T extends Resource> {

    T getResource(String location);

    List<T> getResources(String pattern);

    String getProtocol();
}
