import { Get } from './Get';

import { confirmationReceiptRepository } from 'core/domain/model/ConfirmationReceipt/ConfirmationReceiptRepository';

export const ConfirmationReceiptService = {
  get: Get({ confirmationReceiptRepository })
};
