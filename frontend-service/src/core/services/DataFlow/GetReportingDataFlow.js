export const GetReportingDataFlow = ({ dataFlowRepository }) => async dataFlowId =>
  dataFlowRepository.reporting(dataFlowId);
