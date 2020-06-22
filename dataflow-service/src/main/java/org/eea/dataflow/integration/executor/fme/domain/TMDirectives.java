package org.eea.dataflow.integration.executor.fme.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Gets the tag.
 *
 * @return the tag
 */
@Getter

/**
 * Sets the tag.
 *
 * @param tag the new tag
 */
@Setter

/**
 * To string.
 *
 * @return the java.lang. string
 */
@ToString
public class TMDirectives {

  /**
   * The rtc. Runs a job until it is explicitly canceled. The job will run again regardless of
   * whether the, job completed successfully, failed, or the server crashed or was shut down.
   */
  private Boolean rtc;

  /**
   * The ttc. Time (in seconds) elapsed for a running job before it's cancelled. The minimum value
   * is 1 second, values less than 1 second are ignored
   */
  private Integer ttc;

  /** The ttl. Time to live in the job queue (in seconds). */

  private Integer ttl;

  /** The description. */
  private String description;

  /**
   * The priority. This parameter is available for backward compatibility only, and is no longer
   * supported. Use job routing tag priority instead (see /transformations/jobroutes/tags/*). The
   * priority of the job. Priority values must be integers between 1 and 200. If a request's
   * priority value is less than 1, greater than 200, or is not specified, then FME Server sets it
   * to 100.
   */
  private Integer priority;

  /** The tag. The job routing tag for the request. */
  private String tag;

}
