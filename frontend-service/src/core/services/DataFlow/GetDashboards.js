export const GetDashboards = ({ dataflowRepository }) => async dataflowId => dataflowRepository.dashboards(dataflowId);
