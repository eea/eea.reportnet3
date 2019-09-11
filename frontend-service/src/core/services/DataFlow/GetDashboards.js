export const GetDashboards = ({ dataFlowRepository }) => async dataFlowId => dataFlowRepository.dashboards(dataFlowId);
