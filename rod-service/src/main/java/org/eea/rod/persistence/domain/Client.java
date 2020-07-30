package org.eea.rod.persistence.domain;

import java.io.Serializable;
import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class Client.
 */
@ToString
@Getter
@Setter
public class Client implements Serializable {


  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 6559585358458505926L;

  /** The client id. */
  private Integer clientId;

  /** The name. */
  private String name;

  /** The acronym. */
  private String acronym;

  /** The short name. */
  private String shortName;

  /** The address. */
  private String address;

  /** The url. */
  private String url;

  /** The email. */
  private String email;

  /** The postal code. */
  private String postalCode;

  /** The city. */
  private String city;

  /** The description. */
  private String description;

  /** The country. */
  private String country;

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
    Client client = (Client) o;
    return Objects.equal(shortName, client.shortName) && Objects.equal(address, client.address)
        && Objects.equal(url, client.url) && Objects.equal(email, client.email)
        && Objects.equal(postalCode, client.postalCode) && Objects.equal(city, client.city)
        && Objects.equal(description, client.description) && Objects.equal(country, client.country);
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hashCode(shortName, address, url, email, postalCode, city, description, country);
  }
}
