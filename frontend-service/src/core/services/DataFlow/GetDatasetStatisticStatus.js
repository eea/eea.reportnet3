export const GetDatasetStatisticStatus = ({ dataflowRepository }) => async dataflowId =>
  dataflowRepository.datasetsValidationStatistics(dataflowId);
