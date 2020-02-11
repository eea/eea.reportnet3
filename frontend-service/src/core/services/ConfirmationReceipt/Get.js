export const Get = ({ confirmationReceiptRepository }) => async dataflowId =>
  confirmationReceiptRepository.get(dataflowId);
