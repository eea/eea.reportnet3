package org.eea.interfaces.vo.document;

import java.io.Serializable;
import java.util.Date;
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

  /** The dataflow id. */
  private Long dataflowId;

  /** The size. */
  private Long size;

  /** The date. */
  private Date date;

  /** The is public. */
  private Boolean isPublic;

  /** The is big data. */
  private Boolean isBigData;
}
