package org.eea.dataflow.persistence.domain;

import java.util.Objects;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class DataProvider.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "data_provider")
public class DataProvider {

  /** The id. */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_provider_id_seq")
  @SequenceGenerator(name = "data_provider_id_seq", sequenceName = "data_provider_id_seq",
      allocationSize = 1)
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /** The label. */
  @Column(name = "label")
  private String label;

  /** The code. */
  @Column(name = "code")
  private String code;

  /** The group id. */
  @ManyToOne
  @JoinColumn(name = "group_id")
  private DataProviderGroup dataProviderGroup;

  /** The representatives. */
  @OneToMany(mappedBy = "dataProvider")
  private Set<Representative> representatives;


  /**
   * Equals.
   *
   * @param o the o
   *
   * @return true, if successful
   */
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final DataProvider dataProvider = (DataProvider) o;
    return id.equals(dataProvider.id);

  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, label);
  }
}
