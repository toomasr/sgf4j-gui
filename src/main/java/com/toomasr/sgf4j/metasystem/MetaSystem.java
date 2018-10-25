package com.toomasr.sgf4j.metasystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;
import org.ini4j.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a meta information system for folders of SGF files. Whenever
 * a SGF file is opened the system tries to create a file called
 * 'sgf4j-folder-meta.txt'. The file will hold generic information about the
 * folder. The number of problems, number of unsolved problems, last time problem
 * solved etc.
 *
 * Also there are for each problem a line of sha(1) of the filename and then meta
 * information like when was the problem last opened, last solved and what is
 * the status (None/Easy/Medium/Difficult/Fail).
 */
public class MetaSystem {
  public static final String META_SYSTEM_FILE = "sgf4j-folder-meta.txt";

  private static final String INI_HEADER = "file-hashes";
  private static final Logger logger = LoggerFactory.getLogger(MetaSystem.class);
  private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private Ini ini;

  public MetaSystem(Path path) {
    // if somebody provides a filepath then let's use the parent folder
    if (path.toFile().isFile()) {
      path = path.getParent();
    }

    if (!path.resolve(META_SYSTEM_FILE).toFile().exists()) {
      createMetaSystemFile(path.resolve(META_SYSTEM_FILE));
    }

    parse(path.resolve(META_SYSTEM_FILE));

    if (ini.get(INI_HEADER, "folder.sgfCount") == null) {
      updateFolderGenericNumbers(path);
    }
  }

  private void updateFolderGenericNumbers(Path path) {
    try (Stream<Path> files = Files.list(path).filter(p -> p.toString().toLowerCase().endsWith("sgf"))) {
      ini.put(INI_HEADER, "folder.sgfCount", files.count());
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void parse(Path path) {
    try {
      ini = new Ini(path.toFile());
    }
    catch (IOException e) {
      e.printStackTrace();
      logger.info("Problem parsing file, re-creating " + path.toString());
      createMetaSystemFile(path.resolve(META_SYSTEM_FILE));
    }
  }

  private void createMetaSystemFile(Path metaFilePath) {
    boolean success;
    try {
      success = metaFilePath.toFile().createNewFile();
      if (!success) {
        throw new RuntimeException("Unable to create file " + metaFilePath.toString());
      }
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * https://stackoverflow.com/questions/22463062/how-to-parse-format-dates-with-localdatetime-java-8
   */
  private void updateFileOpened(String key) {
    try {
      String formattedDateTime = LocalDateTime.now().format(dateTimeFormatter);
      ini.put(INI_HEADER, key + ".lastOpened", formattedDateTime);
      // this is the general information for the folder!
      ini.put(INI_HEADER, "folder.lastOpened", formattedDateTime);
      ini.store();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void updateFileStatus(String key, ProblemStatus status) {
    try {
      String formattedDateTime = LocalDateTime.now().format(dateTimeFormatter);
      ProblemStatus prevStatus = getStatus(key);
      if (prevStatus != status) {
        updateFolderStats(prevStatus, status);
        ini.put(INI_HEADER, key + ".status", status.name());
      }
      ini.put(INI_HEADER, key + ".lastSolved", formattedDateTime);
      ini.store();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Decrement the previous status value and increment the newstatus value.
   */
  private void updateFolderStats(ProblemStatus prevStatus, ProblemStatus newStatus) {
    String value = ini.get(INI_HEADER, "folder." + prevStatus.name());
    Integer prevValue = 0;
    try {
      prevValue = Integer.parseInt(value);
    }
    catch (NumberFormatException e) {
    }
    prevValue = prevValue > 0 ? prevValue - 1 : 0;
    ini.put(INI_HEADER, "folder." + prevStatus.name(), prevValue);

    value = ini.get(INI_HEADER, "folder." + newStatus.name());
    Integer newValue = 0;
    try {
      newValue = Integer.parseInt(value);
    }
    catch (NumberFormatException e) {
    }
    newValue++;
    ini.put(INI_HEADER, "folder." + newStatus.name(), newValue);
  }

  private String getLastOpened(String key) {
    return ini.get(INI_HEADER, key + ".lastOpened");
  }

  private String getLastSolved(String key) {
    return ini.get(INI_HEADER, key + ".lastSolved");
  }

  private ProblemStatus getStatus(String key) {
    String status = ini.get(INI_HEADER, key + ".status");
    if (status == null)
      return ProblemStatus.NONE;
    //
    try {
      return Enum.valueOf(ProblemStatus.class, status.trim().toUpperCase());
    }
    catch (IllegalArgumentException e) {
      logger.debug("Unable to find ENUM for value " + status.trim().toUpperCase());
      return ProblemStatus.NONE;
    }
  }

  private static String getKeyForSgf(String sgfPath) {
    return DigestUtils.sha1Hex(sgfPath);
  }

  public static void updateFileOpened(Path sgfPath) {
    MetaSystem ms = new MetaSystem(sgfPath);
    String key = getKeyForSgf(sgfPath.getFileName().toString());
    ms.updateFileOpened(key);
  }

  public static void updateFileStatus(Path sgfPath, ProblemStatus status) {
    MetaSystem ms = new MetaSystem(sgfPath);
    String key = getKeyForSgf(sgfPath.getFileName().toString());
    ms.updateFileStatus(key, status);
  }

  public static LocalDateTime getLastOpened(Path sgfPath) {
    MetaSystem ms = new MetaSystem(sgfPath);
    String key = getKeyForSgf(sgfPath.getFileName().toString());

    if (ms.getLastOpened(key) == null)
      return null;

    LocalDateTime dateTime = LocalDateTime.parse(ms.getLastOpened(key), dateTimeFormatter);
    return dateTime;
  }

  public static LocalDateTime getLastSolved(Path sgfPath) {
    MetaSystem ms = new MetaSystem(sgfPath);
    String key = getKeyForSgf(sgfPath.getFileName().toString());

    if (ms.getLastSolved(key) == null)
      return null;

    LocalDateTime dateTime = LocalDateTime.parse(ms.getLastSolved(key), dateTimeFormatter);
    return dateTime;
  }

  public static ProblemStatus getStatus(Path sgfPath) {
    MetaSystem ms = new MetaSystem(sgfPath);
    String key = getKeyForSgf(sgfPath.getFileName().toString());
    return ms.getStatus(key);
  }

  public static LocalDateTime getFolderInfoLastOpened(Path sgfPath) {
    MetaSystem ms = new MetaSystem(sgfPath);
    if (ms.getFolderInfoLastOpened() == null)
      return null;

    LocalDateTime dateTime = LocalDateTime.parse(ms.getFolderInfoLastOpened(), dateTimeFormatter);
    return dateTime;
  }

  private String getFolderInfoLastOpened() {
    return ini.get(INI_HEADER, "folder.lastOpened");
  }

  public static String getFolderInfoNumberOfProblems(Path sgfPath) {
    MetaSystem ms = new MetaSystem(sgfPath);
    return ms.getFolderInfoNumberOfProblems();
  }

  private String getFolderInfoNumberOfProblems() {
    return ini.get(INI_HEADER, "folder.sgfCount");
  }

  public static Integer getFolderInfoNumberOfFailed(Path sgfPath) {
    MetaSystem ms = new MetaSystem(sgfPath);
    return ms.getFolderInfoNumberOfFailed();
  }

  private Integer getFolderInfoNumberOfFailed() {
    Integer rtrn = 0;
    try {
      rtrn = Integer.parseInt(ini.get(INI_HEADER, "folder.FAIL"));
    }
    catch (NumberFormatException e) {
    }
    return rtrn;
  }

  public static Integer getFolderInfoNumberOfSolved(Path sgfPath) {
    MetaSystem ms = new MetaSystem(sgfPath);
    return ms.getFolderInfoNumberOfSolved();
  }

  private Integer getFolderInfoNumberOfSolved() {
    String easy = ini.get(INI_HEADER, "folder.EASY");
    String medium = ini.get(INI_HEADER, "folder.MEDIUM");
    String difficult = ini.get(INI_HEADER, "folder.DIFFICULT");

    Integer rtrn = 0;
    try {
      rtrn = rtrn + Integer.valueOf(easy);
    }
    catch (NumberFormatException e1) {
    }

    try {
      rtrn = rtrn + Integer.valueOf(medium);
    }
    catch (NumberFormatException e) {}

    try {
      rtrn = rtrn + Integer.valueOf(difficult);
    }
    catch (NumberFormatException e) {
    }

    return rtrn;
  }
}
