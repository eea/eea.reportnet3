export const Create = ({ dataflowRepository }) => async (name, description, obligationId) =>
  dataflowRepository.create(name, description, obligationId);
