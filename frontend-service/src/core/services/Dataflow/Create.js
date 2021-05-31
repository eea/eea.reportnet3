export const Create = ({ dataflowRepository }) => async (name, description, obligationId, type) =>
  dataflowRepository.create(name, description, obligationId, type);
