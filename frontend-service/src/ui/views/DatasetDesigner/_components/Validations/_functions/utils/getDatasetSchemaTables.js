import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

export const getDatasetSchemaTables = (candidateDataset, datasetSchemas) => {
  const [selectedDataset] = datasetSchemas.filter(
    datasetSchema => datasetSchema.datasetSchemaId === candidateDataset.code
  );
  const tables =
    !isNil(selectedDataset) && !isEmpty(selectedDataset) && !isNil(selectedDataset.tables)
      ? selectedDataset.tables.map(table => ({
          label: table.tableSchemaName,
          code: table.tableSchemaId
        }))
      : [];

  return tables;
};
