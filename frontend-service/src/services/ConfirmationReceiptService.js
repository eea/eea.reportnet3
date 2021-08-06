import { ConfirmationReceiptRepository } from 'repositories/ConfirmationReceiptRepository';

const download = async (dataflowId, dataProviderId) =>
  ConfirmationReceiptRepository.download(dataflowId, dataProviderId);

export const ConfirmationReceiptService = { download };
