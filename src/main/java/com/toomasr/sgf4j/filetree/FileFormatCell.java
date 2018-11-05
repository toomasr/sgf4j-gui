package com.toomasr.sgf4j.filetree;

import java.io.File;

import com.toomasr.sgf4j.metasystem.MetaSystem;
import com.toomasr.sgf4j.metasystem.ProblemStatus;

import javafx.scene.control.TreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class FileFormatCell extends TreeCell<File> {
  private Image noneImage = new Image(getClass().getResourceAsStream("/icons/none_16x16.png"));
  private Image failedImage = new Image(getClass().getResourceAsStream("/icons/failed-red_16x16.png"));
  private Image solvedImage = new Image(getClass().getResourceAsStream("/icons/solved_16x16.png"));

  public FileFormatCell() {
    super();
  }

  protected void updateItem(File file, boolean empty) {
    super.updateItem(file, empty);
    if (empty || file == null) {
      setText(null);
      setGraphic(null);
    }
    else {
      /*
       * For a root device on a Mac getName will return an empty string
       * but actually I'd like to show a slash designating the root device
       *
       */
      if ("".equals(file.getName()))
        setText(file.getAbsolutePath());
      /*
       * For SGF files we want to show some custom icons.
       */
      else if (file != null && file.isFile() && file.toString().toLowerCase().endsWith("sgf")) {
        setText(file.getName());
        if (MetaSystem.systemExists(file.toPath())) {
          ProblemStatus status = MetaSystem.getStatus(file.toPath());

          if (status == ProblemStatus.NONE)
            setGraphic(new ImageView(noneImage));
          else if (status == ProblemStatus.FAIL)
            setGraphic(new ImageView(failedImage));
          else
            setGraphic(new ImageView(solvedImage));
        }
        else {
          setGraphic(null);
        }
      }
      else {
        setText(file.getName());
        setGraphic(null);
      }
    }
  }
}
