package org.eea.interfaces.vo.dataset.schemas;

import java.util.Objects;
import org.bson.types.ObjectId;
import org.eea.interfaces.vo.dataset.enums.TypeData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class FieldSchemaVO {


  private ObjectId idFieldSchema;


  private TypeData type;


  @Override
  public int hashCode() {
    return Objects.hash(idFieldSchema, type);
  }


  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    FieldSchemaVO other = (FieldSchemaVO) obj;
    return Objects.equals(idFieldSchema, other.idFieldSchema) && type == other.type;
  }


}
