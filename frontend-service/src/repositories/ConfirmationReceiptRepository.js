import { ConfirmationReceiptConfig } from './config/ConfirmationReceiptConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const ConfirmationReceiptRepository = {
  download: async (dataflowId, dataProviderId) => {
    return await HTTPRequester.download({
      url: getUrl(ConfirmationReceiptConfig.download, { dataflowId, dataProviderId })
    });
  }
};
