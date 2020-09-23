package org.eea.interfaces.vo.rod;


import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Country vo.
 */
@Getter
@Setter
@ToString
public class CountryVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -6017515157081925799L;

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
}
