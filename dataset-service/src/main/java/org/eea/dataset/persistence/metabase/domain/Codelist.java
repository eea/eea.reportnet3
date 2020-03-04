package org.eea.dataset.persistence.metabase.domain;

import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
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
import org.eea.interfaces.vo.dataset.enums.CodelistStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class Codelist.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "CODELIST")
@Deprecated
public class Codelist {

  /** The id. */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "codelist_id_seq")
  @SequenceGenerator(name = "codelist_id_seq", sequenceName = "codelist_id_seq", allocationSize = 1)
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /** The name. */
  @Column(name = "name")
  private String name;

  /** The description. */
  @Column(name = "description")
  private String description;

  /** The category. */
  @ManyToOne
  @JoinColumn(name = "id_category")
  private CodelistCategory category;

  /** The version. */
  @Column(name = "version")
  private String version;

  /** The items. */
  @OneToMany(mappedBy = "codelist", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<CodelistItem> items;

  /** The status. */
  @Column(name = "status")
  private CodelistStatusEnum status;

  /**
   * Equals.
   *
   * @param object the object
   * @return true, if successful
   */
  @Override
  public boolean equals(final Object object) {
    if (this == object) {
      return true;
    }
    if (object == null || getClass() != object.getClass()) {
      return false;
    }
    final Codelist codelist = (Codelist) object;
    return id.equals(codelist.id) && name.equals(codelist.name)
        && description.equals(codelist.description) && version.equals(codelist.version)
        && status.equals(codelist.status);
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, version, status);
  }
}
