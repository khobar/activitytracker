package com.qprogramming.activtytracker.utils;

import com.qprogramming.activtytracker.exceptions.ConfigurationException;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

public class FileUtils {
    public static File getFileBasedOnProperty(String fileProperty) throws ConfigurationException, IOException {
        String filename = System.getProperty(fileProperty);
        if (StringUtils.isEmpty(filename)) {
            throw new ConfigurationException("Failed to find file");
        }
        File file = new File(filename);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }
}
