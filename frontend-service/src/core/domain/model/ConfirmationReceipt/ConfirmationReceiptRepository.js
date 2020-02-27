import { ApiConfirmationReceiptRepository } from 'core/infrastructure/domain/model/ConfirmationReceipt/ApiConfirmationReceiptRepository';

export const ConfirmationReceiptRepository = {
  get: () => Promise.reject('[ConfirmationReceiptDatasetRepository#get] must be implemented')
};

export const confirmationReceiptRepository = Object.assign(
  {},
  ConfirmationReceiptRepository,
  ApiConfirmationReceiptRepository
);
