export const GetDatasetStatisticStatus = ({ dataflowRepository }) => async dataflowId =>
  dataflowRepository.datasetStatisticsStatus(dataflowId);
