import { ConfirmationReceiptConfig } from 'conf/domain/model/ConfirmationReceipt';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

export const apiConfirmationReceipt = {
  get: async dataflowId => {
    const tokens = userStorage.get();

    const response = await HTTPRequester.get({
      url: getUrl(ConfirmationReceiptConfig.get, {
        dataflowId
      }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  }
};
