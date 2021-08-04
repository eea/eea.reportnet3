import { ApiConfirmationReceiptRepository } from 'repositories/_temp/model/ConfirmationReceipt/ApiConfirmationReceiptRepository';

export const ConfirmationReceiptRepository = {
  download: () => Promise.reject('[ConfirmationReceiptDatasetRepository#download] must be implemented')
};

export const confirmationReceiptRepository = Object.assign(
  {},
  ConfirmationReceiptRepository,
  ApiConfirmationReceiptRepository
);
