export const CreateTableRule = ({ validationRepository }) => async (datasetSchemaId, validationRule) =>
  validationRepository.createTableRule(datasetSchemaId, validationRule);
