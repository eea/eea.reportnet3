package org.eea.dataset.persistence.metabase.domain;

import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class CodelistCategory.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "CODELIST_CATEGORY")
public class CodelistCategory {

  /** The id. */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "codelist_category_id_seq")
  @SequenceGenerator(name = "codelist_category_id_seq", sequenceName = "codelist_category_id_seq",
      allocationSize = 1)
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /** The short code. */
  @Column(name = "short_code")
  private String shortCode;

  /** The description. */
  @Column(name = "description")
  private String description;

  /** The codelists. */
  @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Codelist> codelists;

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
    final CodelistCategory codelistCategory = (CodelistCategory) object;
    return id.equals(codelistCategory.id) && shortCode.equals(codelistCategory.shortCode)
        && description.equals(codelistCategory.description);
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, shortCode, description);
  }

}
