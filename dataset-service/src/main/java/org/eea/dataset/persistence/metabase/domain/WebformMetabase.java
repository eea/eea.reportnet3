package org.eea.dataset.persistence.metabase.domain;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class WebForm.
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


  /**
   * The id webFormName.
   */
  @Field(value = "name")
  private String name;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {

    return Objects.hash(name);

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
    return Objects.equals(name, other.name);
  }
}
