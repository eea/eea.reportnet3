export const Delete = ({ validationRepository }) => async (datasetSchemaId, ruleId) =>
  validationRepository.deleteById(datasetSchemaId, ruleId);
