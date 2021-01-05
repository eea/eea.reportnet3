export const UpdateRecord = ({ datasetRepository }) => async (datasetId, record, updateInCascade) =>
  datasetRepository.updateRecordsById(datasetId, record, updateInCascade);
