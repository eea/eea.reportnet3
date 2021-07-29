package org.eea.dataflow.persistence.domain;

import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.eea.interfaces.vo.dataflow.enums.TypeDataProviderEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class DataProviderGroup.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "data_provider_group")
public class DataProviderGroup {

  /** The id. */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_provider_group_id_seq")
  @SequenceGenerator(name = "data_provider_group_id_seq", sequenceName = "data_provider_id_seq",
      allocationSize = 1)
  @Column(name = "id", columnDefinition = "serial")
  private Long id;

  /** The name. */
  @Column(name = "name")
  private String name;

  /** The type. */
  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  private TypeDataProviderEnum type;

  /** The dataproviders. */
  @OneToMany(mappedBy = "dataProviderGroup", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<DataProvider> dataProviders;

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
    final DataProviderGroup dataProviderGroup = (DataProviderGroup) o;
    return id.equals(dataProviderGroup.id);

  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, type, name);
  }
}
