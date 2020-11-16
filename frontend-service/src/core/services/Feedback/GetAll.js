export const GetAll = ({ feedbackRepository }) => async (dataflowId, page, dataProviderId) =>
  feedbackRepository.loadMessages(dataflowId, page, dataProviderId);
