package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.Objects;

public class FieldVO implements Serializable {

  private static final long serialVersionUID = -5257537261370694057L;
  private String name;
  private String id;

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final FieldVO recordVO = (FieldVO) o;
    return name.equals(recordVO.name) &&
        id.equals(recordVO.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, id);
  }

  @Override
  public String toString() {
    return "RecordVO{" +
        "name='" + name + '\'' +
        ", id='" + id + '\'' +
        '}';
  }
}
