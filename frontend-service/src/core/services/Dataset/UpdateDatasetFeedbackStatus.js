export const UpdateDatasetFeedbackStatus = ({ datasetRepository }) => async (dataflowId, datasetId, message, status) =>
  datasetRepository.updateDatasetFeedbackStatus(dataflowId, datasetId, message, status);
