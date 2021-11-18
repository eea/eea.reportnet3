import { ConfirmationReceiptRepository } from 'repositories/ConfirmationReceiptRepository';

export const ConfirmationReceiptService = {
  download: async (dataflowId, dataProviderId) => ConfirmationReceiptRepository.download(dataflowId, dataProviderId)
};
