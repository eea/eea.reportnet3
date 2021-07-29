export const Create = ({ businessDataflowRepository }) => async (name, description, type) =>
  businessDataflowRepository.create(name, description, type);
