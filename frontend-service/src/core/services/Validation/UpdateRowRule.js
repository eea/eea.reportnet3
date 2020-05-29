export const UpdateRowRule = ({ validationRepository }) => async (datasetId, validationRule) =>
  validationRepository.updateRowRule(datasetId, validationRule);
