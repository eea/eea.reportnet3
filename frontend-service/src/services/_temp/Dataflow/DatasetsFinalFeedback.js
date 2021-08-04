export const DatasetsFinalFeedback = ({ dataflowRepository }) => async dataflowId =>
  dataflowRepository.datasetsFinalFeedback(dataflowId);
