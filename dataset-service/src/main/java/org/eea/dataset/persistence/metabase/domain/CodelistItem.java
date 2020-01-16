package org.eea.dataset.persistence.metabase.domain;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
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
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "codelist_item_id_seq")
  @SequenceGenerator(name = "codelist_item_id_seq", sequenceName = "codelist_item_id_seq",
      allocationSize = 1)
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
  @JoinColumn(name = "id_codelist")
  private Codelist codelist;

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
