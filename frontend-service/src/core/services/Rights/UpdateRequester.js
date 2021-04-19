export const UpdateRequester = ({ rightsRepository }) => async (user, dataflowId) =>
  rightsRepository.updateRequester(user, dataflowId);
