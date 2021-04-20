export const DeleteRequester = ({ userRightRepository }) => async (account, dataflowId) =>
  userRightRepository.deleteRequester(account, dataflowId);
