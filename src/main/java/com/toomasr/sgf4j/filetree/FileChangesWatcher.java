package com.toomasr.sgf4j.filetree;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileChangesWatcher implements Runnable {
  private Path path;
  private final AtomicBoolean running = new AtomicBoolean(true);
  private FileTreeView fileTreeView;

  public FileChangesWatcher(FileTreeView fileTreeView, Path path) {
    this.path = path;
    this.fileTreeView = fileTreeView;
  }

  public void stop() {
    running.set(false);
  }

  public Path getPath() {
    return path;
  }

  @Override
  public void run() {
    try (WatchService watcher = FileSystems.getDefault().newWatchService();) {
      path.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
      while (running.get()) {
        WatchKey key = watcher.take();
        for (WatchEvent<?> event : key.pollEvents()) {
          // event.kind()：event type
          if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
            // event may be lost or discarded
            continue;
          }
          // Returns the path (relative path) of the file or directory that triggered the
          // event
          Path fileName = (Path) event.context();
          if (fileName.toString().endsWith("sgf")) {
              fileTreeView.refreshFolder(this.path);
          }
          else {
          }
        }
        // This method needs to be reset every time the take() or poll() method of
        // WatchService is called
        if (!key.reset()) {
          break;
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}