package org.eea.orchestrator.persistence.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

@Data
public class JobStatsDTO {
  private Long totalJobs;
  private Long importJobs;
  private Long validationJobs;
  private Long releaseJobs;
  private Long exportJobs;
  private Long deleteJobs;
  private Long finishedJobs;
  private Long failedJobs;
  private Long refusedJobs;
  private Long canceledJobs;
  private Long canceledByAdminJobs;
  private Long inProgressJobs;
  private Long queuedJobs;

  public JobStatsDTO(Long totalJobs, Long importJobs, Long validationJobs, Long releaseJobs,
                     Long exportJobs, Long deleteJobs, Long finishedJobs, Long failedJobs,
                     Long refusedJobs, Long canceledJobs, Long canceledByAdminJobs,
                     Long inProgressJobs, Long queuedJobs) {
    this.totalJobs = totalJobs;
    this.importJobs = importJobs;
    this.validationJobs = validationJobs;
    this.releaseJobs = releaseJobs;
    this.exportJobs = exportJobs;
    this.deleteJobs = deleteJobs;
    this.finishedJobs = finishedJobs;
    this.failedJobs = failedJobs;
    this.refusedJobs = refusedJobs;
    this.canceledJobs = canceledJobs;
    this.canceledByAdminJobs = canceledByAdminJobs;
    this.inProgressJobs = inProgressJobs;
    this.queuedJobs = queuedJobs;
  }

  @Override
  public String toString() {
    ObjectMapper mapper = new ObjectMapper();
    String json = null;
    try {
      json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    return json;
  }
}
