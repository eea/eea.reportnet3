export const DeleteRequester = ({ rightsRepository }) => async (account, dataflowId, dataProviderId) =>
  rightsRepository.deleteRequester(account, dataflowId, dataProviderId);
