export const Delete = ({ integrationRepository }) => async (dataflowId, integrationId) =>
  integrationRepository.deleteById(dataflowId, integrationId);
