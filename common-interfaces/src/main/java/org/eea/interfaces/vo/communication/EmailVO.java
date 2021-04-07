package org.eea.interfaces.vo.communication;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;



/**
 * The Class EmailVO.
 */
@Getter
@Setter
@ToString
public class EmailVO {

  /** The to. */
  private List<String> to;

  /** The bbc. */
  private List<String> bbc;

  /** The cc. */
  private List<String> cc;

  /** The subject. */
  private String subject;

  /** The text. */
  private String text;

}
