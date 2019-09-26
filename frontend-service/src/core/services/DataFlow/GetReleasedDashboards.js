export const GetReleasedDashboards = ({ dataFlowRepository }) => async dataFlowId =>
  dataFlowRepository.datasetReleasedStatus(dataFlowId);
