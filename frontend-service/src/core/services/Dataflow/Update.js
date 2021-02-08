export const Update = ({ dataflowRepository }) => async (dataflowId, name, description, obligationId, isReleaseable) =>
  dataflowRepository.update(dataflowId, name, description, obligationId, isReleaseable);
