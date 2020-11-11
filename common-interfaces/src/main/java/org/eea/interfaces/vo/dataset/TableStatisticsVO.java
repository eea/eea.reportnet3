package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The class TableStatisticsVO.
 *
 */
@Setter
@Getter
@ToString
public class TableStatisticsVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 7063146120694063716L;


  /** The id table schema. */
  private String idTableSchema;

  /** The name table schema. */
  private String nameTableSchema;

  /** The table errors. */
  private Boolean tableErrors;

  /** The total errors. */
  private Long totalErrors;

  /** The total records. */
  private Long totalRecords;

  /** The total records with blockers. */
  private Long totalRecordsWithBlockers;

  /** The total records with errors. */
  private Long totalRecordsWithErrors;

  /** The total records with warnings. */
  private Long totalRecordsWithWarnings;

  /** The total records with Infos. */
  private Long totalRecordsWithInfos;



  /**
   * Instantiates a new table statistics VO.
   */
  public TableStatisticsVO() {
    super();
  }


  /**
   * Instantiates a new table statistics VO.
   *
   * @param idTableSchema the id table schema
   * @param nameTableSchema the name table schema
   */
  public TableStatisticsVO(final String idTableSchema, final String nameTableSchema) {

    this.idTableSchema = idTableSchema;
    this.nameTableSchema = nameTableSchema;
    this.tableErrors = false;
    this.totalErrors = 0L;
    this.totalRecords = 0L;
    this.totalRecordsWithErrors = 0L;
    this.totalRecordsWithWarnings = 0L;

  }


  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(idTableSchema, nameTableSchema, tableErrors, totalErrors, totalRecords,
        totalRecordsWithErrors, totalRecordsWithWarnings);
  }


  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final TableStatisticsVO other = (TableStatisticsVO) obj;
    return Objects.equals(idTableSchema, other.idTableSchema)
        && Objects.equals(nameTableSchema, other.nameTableSchema)
        && Objects.equals(tableErrors, other.tableErrors)
        && Objects.equals(totalErrors, other.totalErrors)
        && Objects.equals(totalRecordsWithErrors, other.totalRecordsWithErrors)
        && Objects.equals(totalRecordsWithWarnings, other.totalRecordsWithWarnings);
  }

}
