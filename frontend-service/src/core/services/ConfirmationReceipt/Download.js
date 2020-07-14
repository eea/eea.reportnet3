export const Download = ({ confirmationReceiptRepository }) => async (dataflowId, dataProviderId) =>
  confirmationReceiptRepository.download(dataflowId, dataProviderId);
