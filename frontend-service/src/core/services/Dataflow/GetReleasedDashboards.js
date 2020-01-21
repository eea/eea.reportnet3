export const GetReleasedDashboards = ({ dataflowRepository }) => async dataflowId =>
  dataflowRepository.datasetsReleasedStatus(dataflowId);
