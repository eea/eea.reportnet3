export const CreateDatasetRule = ({ validationRepository }) => async (datasetSchemaId, validationRule) =>
  validationRepository.createDatasetRule(datasetSchemaId, validationRule);
