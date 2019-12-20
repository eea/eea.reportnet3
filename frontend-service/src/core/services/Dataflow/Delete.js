export const Delete = ({ dataflowRepository }) => async dataflowId => dataflowRepository.deleteById(dataflowId);
