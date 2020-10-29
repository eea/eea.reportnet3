export const UpdateRead = ({ feedbackRepository }) => async (dataflowId, messageIds, read) =>
  feedbackRepository.markAsRead(dataflowId, messageIds, read);
