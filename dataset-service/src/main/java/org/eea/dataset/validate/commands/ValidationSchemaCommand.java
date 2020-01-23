package org.eea.dataset.validate.commands;

import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;

/**
 * The Interface ValidationSchemaCommand.
 */
public interface ValidationSchemaCommand {

  /**
   * Execute.
   *
   * @param schema the schema
   * @return the boolean
   */
  Boolean execute(DataSetSchemaVO schema);

}
