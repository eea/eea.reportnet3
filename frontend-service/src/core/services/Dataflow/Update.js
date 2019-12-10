export const Update = ({ dataflowRepository }) => async (name, description, dataflowId) =>
  dataflowRepository.update(name, description, dataflowId);
