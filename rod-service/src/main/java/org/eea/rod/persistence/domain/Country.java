package org.eea.rod.persistence.domain;

import com.google.common.base.Objects;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class Country implements Serializable {

  private static final long serialVersionUID = 530170819412036107L;
  private Integer spatialId;
  private String name;
  private String type;
  private String twoLetter;
  private String memberCountry;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Country country = (Country) o;
    return Objects.equal(spatialId, country.spatialId) &&
        Objects.equal(name, country.name) &&
        Objects.equal(type, country.type) &&
        Objects.equal(twoLetter, country.twoLetter) &&
        Objects.equal(memberCountry, country.memberCountry);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(spatialId, name, type, twoLetter, memberCountry);
  }
}
