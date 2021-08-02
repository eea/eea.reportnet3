import { getEmptyLink } from './getEmptyLink';

export const initValidationRuleRelationCreation = (rawTables, datasetSchemaId, datasetSchemas) => {
  const tables = rawTables
    .filter(table => !table.addTab)
    .map(table => {
      return { label: table.header, code: table.tableSchemaId };
    });

  const datasets = datasetSchemas.map(datasetSchema => {
    return { label: datasetSchema.datasetSchemaName, code: datasetSchema.datasetSchemaId };
  });

  const newLink = getEmptyLink();
  return {
    tables,
    datasetSchemas: datasets,
    candidateRule: {
      table: undefined,
      field: undefined,
      shortCode: '',
      name: '',
      description: '',
      errorMessage: '',
      errorLevel: undefined,
      active: true,
      expressions: [],
      allExpressions: [],
      relations: {
        isDoubleReferenced: false,
        referencedDatasetSchema: {},
        referencedFields: [],
        referencedTable: {},
        referencedTables: [],
        originDatasetSchema: datasetSchemaId,
        links: [newLink]
      }
    }
  };
};
