import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';

export const getDatasetSchemaTableFieldsBySchema = (candidateTable, datasetSchemas, datasetSchemaById) => {
  const [selectedDataset] = datasetSchemas.filter(datasetSchema => datasetSchema.datasetSchemaId === datasetSchemaById);
  if (!isNil(selectedDataset) && !isEmpty(selectedDataset) && !isNil(selectedDataset.tables)) {
    const [selectedTable] = selectedDataset.tables.filter(table => table.tableSchemaId === candidateTable.code);
    const fields =
      !isNil(selectedTable) && !isEmpty(selectedTable) && !isNil(selectedTable.records)
        ? selectedTable.records[0].fields
            .filter(field => !config.validations.bannedFields.includes(field.name))
            .map(field => ({
              label: field.name,
              code: field.fieldId
            }))
        : [];
    console.log('fields', fields);
    return fields;
  } else {
    return [];
  }
};
