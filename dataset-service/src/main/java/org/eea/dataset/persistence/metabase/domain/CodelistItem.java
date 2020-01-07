package org.eea.dataset.persistence.metabase.domain;

import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class CodelistItem.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "CODELIST_ITEM")
public class CodelistItem {

  /** The id. */
  @Id
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /** The short code. */
  @Column(name = "short_code")
  private String shortCode;

  /** The label. */
  @Column(name = "label")
  private String label;

  /** The definition. */
  @Column(name = "definition")
  private String definition;

  @ManyToOne
  @JoinColumn(name = "id")
  private Codelist codelist;

  /**
   * The table values.
   */
  @OneToMany(mappedBy = "id", cascade = CascadeType.ALL, orphanRemoval = true)
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
    final CodelistItem codelistItem = (CodelistItem) object;
    return id.equals(codelistItem.id) && shortCode.equals(codelistItem.shortCode)
        && definition.equals(codelistItem.definition) && label.equals(codelistItem.label);
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, shortCode, definition, label);
  }

}
