import { ConfirmationReceiptConfig } from 'conf/domain/model/ConfirmationReceipt';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiConfirmationReceipt = {
  download: async (dataflowId, dataProviderId) => {
    return await HTTPRequester.download({
      url: getUrl(ConfirmationReceiptConfig.download, { dataflowId, dataProviderId })
    });
  }
};
