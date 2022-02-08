package org.eea.dataset.persistence.metabase.domain;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.eea.interfaces.vo.dataset.enums.WebformTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class WebformMetabase.
 */
@Setter
@Getter
@ToString
@Entity
@Table(name = "webform", schema = "public")
public class WebformMetabase implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 7063146120694063716L;

  /**
   * The id.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "webform_id_seq")
  @SequenceGenerator(name = "webform_id_seq", sequenceName = "webform_id_seq", allocationSize = 1)
  @Column(name = "id", columnDefinition = "serial")
  private Long id;

  /** The label. */
  @Column(name = "label")
  private String label;

  /** The value. */
  @Column(name = "value")
  private String value;

  /** The type. */
  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  private WebformTypeEnum type;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {

    return Objects.hash(id, label, value, type);

  }

  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    WebformMetabase other = (WebformMetabase) obj;
    return Objects.equals(label, other.label) && Objects.equals(value, other.value)
        && Objects.equals(type, other.type);
  }
}
