package com.toomasr.sgf4j.metasystem;

public enum ProblemStatus {
  NONE(0), EASY(1), MEDIUM(2), DIFFICULT(3), FAIL(4);

  int status;

  private ProblemStatus(int status) {
    this.status = status;
  }
}
