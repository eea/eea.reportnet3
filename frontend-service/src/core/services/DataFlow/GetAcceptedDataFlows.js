export const GetAcceptedDataFlows = ({ dataFlowRepository }) => async userId => dataFlowRepository.accepted(userId);
