export const Create = ({ validationRepository }) => async (datasetSchemaId, validationRule) =>
  validationRepository.create(datasetSchemaId, validationRule);
