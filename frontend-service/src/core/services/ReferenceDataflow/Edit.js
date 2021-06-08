export const Edit = ({ referenceDataflowRepository }) => async (dataflowId, description, name, type) =>
  referenceDataflowRepository.edit(dataflowId, description, name, type);
