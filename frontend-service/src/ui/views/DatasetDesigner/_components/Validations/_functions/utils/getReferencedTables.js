import isUndefined from 'lodash/isUndefined';

export const getReferencedTables = (datasetSchema, datasetSchemas) => {
  const rawTables = datasetSchemas.filter(dsSchema => dsSchema.datasetSchemaId === datasetSchema.code)[0];

  if (!isUndefined(rawTables)) {
    const tables = rawTables.tables.map(table => {
      return { label: table.tableSchemaName, code: table.tableSchemaId };
    });

    return {
      candidateRule: {
        relations: { referencedDatasetSchema: datasetSchema, referencedTables: tables }
      }
    };
  } else {
    return {
      candidateRule: {
        relations: {
          referencedDatasetSchema: datasetSchema,
          referencedTables: []
        }
      }
    };
  }
};
