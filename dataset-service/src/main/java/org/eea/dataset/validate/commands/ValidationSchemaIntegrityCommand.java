package org.eea.dataset.validate.commands;

import org.eea.interfaces.vo.dataflow.enums.TypeDataflowEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.springframework.stereotype.Component;

/**
 * The Class ValidationSchemaIntegrityCommand. Validates the schema given: checks if at least there
 * is one table, and each table of the schema has at least one field
 */
@Component
public class ValidationSchemaIntegrityCommand implements ValidationSchemaCommand {

  /**
   * Execute.
   *
   * @param schema the schema
   * @param dataflowType the dataflow type
   * @return the boolean
   */
  @Override
  public Boolean execute(DataSetSchemaVO schema, TypeDataflowEnum dataflowType) {

    Boolean isValid = true;
    if (!TypeDataflowEnum.REFERENCE.equals(dataflowType)
        && Boolean.TRUE.equals(schema.getReferenceDataset())) {
      isValid = null;
    } else {
      // This validation consists in: the schema has to have at least one table, and for each table,
      // it's need to be at least one field
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
    }
    return isValid;
  }

}
