package org.eea.interfaces.vo.document;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class DocumentVO.
 */
@Getter
@Setter
@ToString
public class DocumentVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -4265958430236835829L;

  /** The id. */
  private Long id;

  /** The name. */
  private String name;

  /** The language. */
  private String language;

  /** The description. */
  private String description;

  /** The category. */
  private String category;

}
