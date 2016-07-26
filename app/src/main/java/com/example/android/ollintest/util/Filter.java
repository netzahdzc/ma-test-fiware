package com.example.android.ollintest.util;

/**
 * Created by netzahdzc on 7/26/16.
 */

import java.io.File;
import java.io.FilenameFilter;

public class Filter {

    public File[] finder(String dirName, final String extension) {
        File dir = new File(dirName);

        return dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return filename.endsWith("." + extension);
            }
        });

    }

}
