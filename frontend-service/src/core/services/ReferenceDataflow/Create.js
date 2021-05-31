export const Create = ({ referenceDataflowRepository }) => async (name, description, type) =>
  referenceDataflowRepository.create(name, description, type);
