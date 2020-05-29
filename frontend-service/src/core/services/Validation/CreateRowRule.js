export const CreateRowRule = ({ validationRepository }) => async (datasetSchemaId, validationRule) =>
  validationRepository.createRowRule(datasetSchemaId, validationRule);
