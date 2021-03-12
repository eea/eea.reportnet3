import { apiConfirmationReceipt } from 'core/infrastructure/api/domain/model/ConfirmationReceipt/ApiConfirmationReceipt';

const download = async (dataflowId, dataProviderId) => {
  return await apiConfirmationReceipt.download(dataflowId, dataProviderId);
};

export const ApiConfirmationReceiptRepository = { download };
