export const ValidateSqlRules = ({ datasetRepository }) => async (datasetId, datasetSchemaId) =>
  datasetRepository.validateSqlRules(datasetId, datasetSchemaId);
