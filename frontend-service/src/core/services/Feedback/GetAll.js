export const GetAll = ({ feedbackRepository }) => async (dataflowId, page) =>
  feedbackRepository.loadMessages(dataflowId, page);
