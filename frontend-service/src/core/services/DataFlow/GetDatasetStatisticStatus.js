export const GetDatasetStatisticStatus = ({ dataFlowRepository }) => async dataFlowId =>
  dataFlowRepository.datasetStatisticsStatus(dataFlowId);
