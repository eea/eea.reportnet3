export const GetPendingDataFlows = ({ dataFlowRepository }) => async () => dataFlowRepository.pending();
