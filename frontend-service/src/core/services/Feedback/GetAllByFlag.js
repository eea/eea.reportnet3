export const GetAllByFlag = ({ feedbackRepository }) => async (dataflowId, page, read) =>
  feedbackRepository.loadMessagesByFlag(dataflowId, page, read);
