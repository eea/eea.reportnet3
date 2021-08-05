import { confirmationReceiptRepository } from 'repositories/ConfirmationReceiptRepository';

const download = async (dataflowId, dataProviderId) =>
  confirmationReceiptRepository.download(dataflowId, dataProviderId);

export const ConfirmationReceiptService = { download };
