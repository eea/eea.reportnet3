export const UpdateDatasetFeedbackStatus = ({ datasetRepository }) => async (
  dataflowId,
  datasetId,
  message,
  feedbackStatus
) => datasetRepository.updateDatasetFeedbackStatus(dataflowId, datasetId, message, feedbackStatus);
