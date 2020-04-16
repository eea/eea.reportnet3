package org.eea.interfaces.vo.rod;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Country vo.
 */
@Getter
@Setter
@ToString
public class CountryVO {

  private Integer spatialId;
  private String name;
  private String type;
  private String twoLetter;
  private String memberCountry;
}
