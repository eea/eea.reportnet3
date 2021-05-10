export const UpdateRequester = ({ userRightRepository }) => async (userRight, dataflowId) =>
  userRightRepository.updateRequester(userRight, dataflowId);
