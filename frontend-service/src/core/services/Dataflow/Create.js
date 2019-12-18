export const Create = ({ dataflowRepository }) => async (name, description) =>
  dataflowRepository.create(name, description);
