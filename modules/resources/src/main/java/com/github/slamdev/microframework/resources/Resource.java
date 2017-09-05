package com.github.slamdev.microframework.resources;

import java.io.InputStream;

public interface Resource {

    InputStream getInputStream();

    String getName();

    boolean delete();

    String getPath();
}
