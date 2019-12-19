export const Update = ({ dataflowRepository }) => async (dataflowId, name, description) =>
  dataflowRepository.update(dataflowId, name, description);
