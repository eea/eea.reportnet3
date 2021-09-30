package org.eea.interfaces.vo.weblink;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class WeblinkVO.
 */
@Getter
@Setter
@ToString
public class WeblinkVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 4646157396217092042L;

  /** The id. */
  private Long id;

  /** The name. */
  private String description;

  /** The url. */
  private String url;

  /** The is public. */
  private Boolean isPublic;

}
