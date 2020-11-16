export const GetAllByFlag = ({ feedbackRepository }) => async (dataflowId, page, read, dataProviderId) =>
  feedbackRepository.loadMessagesByFlag(dataflowId, page, read, dataProviderId);
