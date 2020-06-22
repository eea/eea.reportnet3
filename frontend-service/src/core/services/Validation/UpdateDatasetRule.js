export const UpdateDatasetRule = ({ validationRepository }) => async (datasetId, validationRule) =>
  validationRepository.updateDatasetRule(datasetId, validationRule);
