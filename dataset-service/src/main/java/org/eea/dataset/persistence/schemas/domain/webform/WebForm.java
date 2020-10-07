package org.eea.dataset.persistence.schemas.domain.webform;

import java.io.Serializable;
import java.util.Objects;
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
public class WebForm implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 7063146120694063716L;

  /**
   * The id webFormName.
   */
  @Field(value = "webFormName")
  private String webFormName;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {

    return Objects.hash(webFormName);

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
    WebForm other = (WebForm) obj;
    return Objects.equals(webFormName, other.webFormName);
  }
}
