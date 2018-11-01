package com.salmon.oss.common.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MimeUtils {

  private static Map<String, String> mimeMap = new HashMap<>();

  static {
    try {
      InputStream inputStream = MimeUtils.class.getResourceAsStream("/mime.types");
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
      String line;
      while ((line = reader.readLine()) != null) {
        String[] ss = line.split("\\s+", 2);
        String ext = ss[1].trim();
        String[] exts = ext.substring(0, ext.length() - 1).split("\\s+");
        for (String extension : exts) {
          mimeMap.put(extension, ss[0].trim());
        }
      }
    } catch (IOException e) {
      //nothing to do
    }

  }

  public static String getFileMimeType(String extension) {
    return mimeMap.get(extension);
  }

  public static String getFileMimeType(File file) {
    String name = file.getName();
    String mine = "application/octet-stream";
    if (name.lastIndexOf(".") > 0) {
      String ext = name.substring(name.lastIndexOf(".") + 1);
      String extMime = getFileMimeType(ext);
      if (extMime != null) {
        mine = extMime;
      }
    }
    return mine;
  }
}
