export const Edit = ({ referenceDataflowRepository }) => async (dataflowId, description, name) =>
  referenceDataflowRepository.edit(dataflowId, description, name);
