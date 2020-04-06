export const Update = ({ dataflowRepository }) => async (dataflowId, name, description, obligationId) =>
  dataflowRepository.update(dataflowId, name, description, obligationId);
