package org.eea.dataset.validate.commands;

import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.springframework.stereotype.Component;

/**
 * The Class ValidationSchemaIntegrityCommand.
 */
@Component
public class ValidationSchemaIntegrityCommand implements ValidationSchemaCommand {

  /**
   * Execute.
   *
   * @param schema the schema
   * @return the boolean
   */
  @Override
  public Boolean execute(DataSetSchemaVO schema) {

    Boolean isValid = true;

    if (schema.getTableSchemas() == null || schema.getTableSchemas().isEmpty()) {
      isValid = false;
    }
    if (schema.getTableSchemas() != null) {
      for (TableSchemaVO table : schema.getTableSchemas()) {
        if (table.getRecordSchema() == null || table.getRecordSchema().getFieldSchema() == null
            || table.getRecordSchema().getFieldSchema().isEmpty()) {
          isValid = false;
          break;
        }
      }
    }
    return isValid;
  }

}
