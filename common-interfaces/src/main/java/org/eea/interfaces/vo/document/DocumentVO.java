package org.eea.interfaces.vo.document;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DocumentVO implements Serializable {

  private static final long serialVersionUID = -4265958430236835829L;

  private Long id;

  private String name;

  private String language;

}
