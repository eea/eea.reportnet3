export const DeleteEditor = ({ rightsRepository }) => async (account, dataflowId, dataProviderId) =>
  rightsRepository.deleteEditor(account, dataflowId, dataProviderId);
