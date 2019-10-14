package org.eea.dataset.persistence.data.domain;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class Statistics.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "STATISTICS")
public class Statistics {

  /** The id. */
  @Id
  @SequenceGenerator(name = "statistics_sequence_generator", sequenceName = "statistics_sequence",
      allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "statistics_sequence_generator")
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /** The stat name. */
  @Column(name = "STAT_NAME")
  private String statName;

  /** The id table schema. */
  @Column(name = "ID_TABLE_SCHEMA")
  private String idTableSchema;

  /** The value. */
  @Column(name = "VALUE")
  private String value;


  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, statName, idTableSchema, value);
  }

  /**
   * Equals.
   *
   * @param obj the obj
   *
   * @return true, if successful
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Statistics other = (Statistics) obj;
    return Objects.equals(id, other.id) && Objects.equals(statName, other.statName)
        && Objects.equals(idTableSchema, other.idTableSchema) && Objects.equals(value, other.value);
  }


}
