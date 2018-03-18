package com.qprogramming.activtytracker.utils;

import com.qprogramming.activtytracker.exceptions.ConfigurationException;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

public class FileUtils {

    public static File getFile(String fileName) throws ConfigurationException, IOException {
        if (StringUtils.isEmpty(fileName)) {
            throw new ConfigurationException("Failed to find file");
        }
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }
}
