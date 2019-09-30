export const GetReleasedDashboards = ({ dataflowRepository }) => async dataflowId =>
  dataflowRepository.datasetReleasedStatus(dataflowId);
