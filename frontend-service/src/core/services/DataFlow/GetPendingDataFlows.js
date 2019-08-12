export const GetPendingDataFlows = ({ dataFlowRepository }) => async userId => dataFlowRepository.pending(userId);
