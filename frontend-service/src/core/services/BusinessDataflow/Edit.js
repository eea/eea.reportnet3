export const Edit = ({ businessDataflowRepository }) => async (dataflowId, description, name, type) =>
  businessDataflowRepository.edit(dataflowId, description, name, type);
