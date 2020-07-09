import { ConfirmationReceiptConfig } from 'conf/domain/model/ConfirmationReceipt';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiConfirmationReceipt = {
  get: async (dataflowId, dataProviderId) => {
    const response = await HTTPRequester.download({
      url: getUrl(ConfirmationReceiptConfig.get, {
        dataflowId,
        dataProviderId
      })
    });

    return response.data;
  }
};
