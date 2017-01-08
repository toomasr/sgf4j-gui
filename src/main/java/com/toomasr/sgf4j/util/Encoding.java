package com.toomasr.sgf4j.util;

import java.awt.Font;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Encoding {
  private static final Logger logger = LoggerFactory.getLogger(Encoding.class);
  public static String determineEncoding(Path pathToSgf, Font font) {

    try {
      byte[] bytes = Files.readAllBytes(pathToSgf);
      String str = new String(bytes, "utf-8");
      for (int i = 0; i < str.length(); i++) {
        char ch = str.charAt(i);
        if (!font.canDisplay(ch)) {
          logger.debug("Cannot display character '{}'", ch);
          return "windows-1252";
        }
      }

    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return "utf-8";
  }

}
