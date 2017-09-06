package com.github.slamdev.microframework.resources;

import lombok.experimental.UtilityClass;

import static com.github.slamdev.microframework.resources.AntPathMatcher.isPattern;

@UtilityClass
public class FilenameUtils {

    static String getFilename(String path) {
        int separatorIndex = path.lastIndexOf('/');
        return separatorIndex == -1 ? path : path.substring(separatorIndex + 1);
    }

    static String determineRootDir(String location) {
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
}
