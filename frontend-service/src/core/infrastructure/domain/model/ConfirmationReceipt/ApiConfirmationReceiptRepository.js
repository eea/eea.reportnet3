import { apiConfirmationReceipt } from 'core/infrastructure/api/domain/model/ConfirmationReceipt/ApiConfirmationReceipt';

const get = async (dataflowId, dataProviderId) => {
  const confirmationReceipt = await apiConfirmationReceipt.get(dataflowId, dataProviderId);

  return confirmationReceipt;
};

export const ApiConfirmationReceiptRepository = {
  get
};
