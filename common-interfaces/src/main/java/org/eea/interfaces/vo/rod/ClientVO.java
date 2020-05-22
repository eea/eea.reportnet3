package org.eea.interfaces.vo.rod;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Client vo.
 */
@Getter
@Setter
@ToString
public class ClientVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 634917579989989107L;

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
}
