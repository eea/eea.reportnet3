export const GetUserList = ({ dataflowRepository }) => async (dataflowId, representativeId) =>
  dataflowRepository.getUserList(dataflowId, representativeId);
