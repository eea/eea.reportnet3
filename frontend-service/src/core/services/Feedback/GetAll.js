export const GetAll = ({ feedbackRepository }) => async (dataflowId, page) =>
  feedbackRepository.loadAllMessages(dataflowId, page);
