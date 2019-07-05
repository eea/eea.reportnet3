package org.eea.interfaces.vo.weblink;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WeblinkVO implements Serializable {

  private static final long serialVersionUID = 4646157396217092042L;

  private Long id;

  private String name;

  private String url;

}
