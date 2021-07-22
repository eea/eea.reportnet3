export const UpdateReferenceDatasetStatus = ({ datasetRepository }) => async (datasetId, updatable) =>
  datasetRepository.updateReferenceDatasetStatus(datasetId, updatable);
