package org.eea.interfaces.vo.rod;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ClientVO {

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
}
