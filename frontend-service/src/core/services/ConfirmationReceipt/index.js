import { Download } from './Download';

import { confirmationReceiptRepository } from 'core/domain/model/ConfirmationReceipt/ConfirmationReceiptRepository';

export const ConfirmationReceiptService = { download: Download({ confirmationReceiptRepository }) };
