export const GetDatasetStatisticStatus = ({ dataflowRepository }) => async dataflowId =>
  dataflowRepository.datasetValidationStatistics(dataflowId);
