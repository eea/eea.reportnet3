export const UpdateEditor = ({ userRightRepository }) => async (userRight, dataflowId) =>
  userRightRepository.updateEditor(userRight, dataflowId);
