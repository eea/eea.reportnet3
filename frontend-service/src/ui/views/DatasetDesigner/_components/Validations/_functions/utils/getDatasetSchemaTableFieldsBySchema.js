import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';

export const getDatasetSchemaTableFieldsBySchema = (candidateTable, datasetSchemas, datasetSchemaById) => {
  const [selectedDataset] = datasetSchemas.filter(datasetSchema => datasetSchema.datasetSchemaId === datasetSchemaById);
  if (!isNil(selectedDataset) && !isEmpty(selectedDataset) && !isNil(selectedDataset.tables)) {
    const [selectedTable] = selectedDataset.tables.filter(table => table.tableSchemaId === candidateTable.code);
    if (!isNil(selectedTable) && !isEmpty(selectedTable) && !isNil(selectedTable.records)) {
      return selectedTable.records[0].fields
        .filter(field => !config.validations.bannedTypes.nonSql.includes(field.type.toLowerCase()))
        .map(field => ({
          label: field.name,
          code: field.fieldId
        }));
    }
    return [];
  }
  return [];
};
