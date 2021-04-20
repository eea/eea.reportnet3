export const DeleteEditor = ({ userRightRepository }) => async (account, dataflowId) =>
  userRightRepository.deleteEditor(account, dataflowId);
