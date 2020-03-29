package org.eea.rod.persistence.domain;

import com.google.common.base.Objects;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@EqualsAndHashCode
public class Client implements Serializable {


  private static final long serialVersionUID = 6559585358458505926L;
  private Integer clientId;
  private String name;
  private String acronym;
  private String shortName;
  private String address;
  private String url;
  private String email;
  private String postalCode;
  private String city;
  private String description;
  private String country;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Client client = (Client) o;
    return Objects.equal(shortName, client.shortName) &&
        Objects.equal(address, client.address) &&
        Objects.equal(url, client.url) &&
        Objects.equal(email, client.email) &&
        Objects.equal(postalCode, client.postalCode) &&
        Objects.equal(city, client.city) &&
        Objects.equal(description, client.description) &&
        Objects.equal(country, client.country);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(shortName, address, url, email, postalCode, city, description, country);
  }
}
