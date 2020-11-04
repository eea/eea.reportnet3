export const Create = ({ feedbackRepository }) => async (dataflowId, message, providerId) =>
  feedbackRepository.create(dataflowId, message, providerId);
