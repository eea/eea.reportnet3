export const GetCompletedDataFlows = ({ dataFlowRepository }) => async userId => dataFlowRepository.completed(userId);
