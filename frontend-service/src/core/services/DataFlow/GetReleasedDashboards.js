export const GetReleasedDashboards = ({ dataFlowRepository }) => async dataFlowId => {
  dataFlowRepository.dataset_status(dataFlowId);
  console.log('in GetReleasedDashboards dataFlowId', dataFlowId);
  console.log('in GetReleasedDashboards dataFlowId dataFlowRepository', dataFlowRepository);
};
