export const UpdateRead = ({ feedbackRepository }) => async (dataflowId, messages) =>
  feedbackRepository.markAsRead(dataflowId, messages);
