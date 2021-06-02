export const Create = ({ referencedDataflowRepository }) => async (name, description, type) =>
  referencedDataflowRepository.create(name, description, type);
