package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RecordVO implements Serializable {

  private static final long serialVersionUID = -5257537261370694057L;
  private String name;
  private String id;

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final RecordVO recordVO = (RecordVO) o;
    return name.equals(recordVO.name) && id.equals(recordVO.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, id);
  }

}
