export const Update = ({ validationRepository }) => async (datasetId, validationRule) =>
  validationRepository.update(datasetId, validationRule);
