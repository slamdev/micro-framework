package com.github.slamdev.microframework.resources;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FilenameUtils {

    static String getFilename(String path) {
        int separatorIndex = path.lastIndexOf('/');
        return separatorIndex == -1 ? path : path.substring(separatorIndex + 1);
    }
}
