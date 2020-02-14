export const Get = ({ confirmationReceiptRepository }) => async (dataflowId, dataProviderId) =>
  confirmationReceiptRepository.get(dataflowId, dataProviderId);
