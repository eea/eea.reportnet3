package org.eea.dataset.service.helper;

import lombok.Getter;

import java.io.File;
import java.util.List;

@Getter
public class CsvModification {
  private final List<File> modifiedCsvFiles;
  private final List<String> csvHeaders;

  public CsvModification(List<File> modifiedCsvFiles, List<String> csvHeaders) {
    this.modifiedCsvFiles = modifiedCsvFiles;
    this.csvHeaders = csvHeaders;
  }
}
