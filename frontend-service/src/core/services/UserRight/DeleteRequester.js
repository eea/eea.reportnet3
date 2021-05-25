export const DeleteRequester = ({ userRightRepository }) => async (userRight, dataflowId) =>
  userRightRepository.deleteRequester(userRight, dataflowId);
