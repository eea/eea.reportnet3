export const Update = ({ dataflowRepository }) => async (dataflowId, name, description, obligationId, isReleasable) =>
  dataflowRepository.update(dataflowId, name, description, obligationId, isReleasable);
