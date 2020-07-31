package org.eea.rod.persistence.domain;

import java.io.Serializable;
import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class Country.
 */
@ToString
@Getter
@Setter
public class Country implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 530170819412036107L;

  /** The spatial id. */
  private Integer spatialId;

  /** The name. */
  private String name;

  /** The type. */
  private String type;

  /** The two letter. */
  private String twoLetter;

  /** The member country. */
  private String memberCountry;

  /**
   * Equals.
   *
   * @param o the o
   * @return true, if successful
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Country country = (Country) o;
    return Objects.equal(spatialId, country.spatialId) && Objects.equal(name, country.name)
        && Objects.equal(type, country.type) && Objects.equal(twoLetter, country.twoLetter)
        && Objects.equal(memberCountry, country.memberCountry);
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hashCode(spatialId, name, type, twoLetter, memberCountry);
  }
}
