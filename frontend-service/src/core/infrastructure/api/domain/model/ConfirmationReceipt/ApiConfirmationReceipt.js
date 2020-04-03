import { ConfirmationReceiptConfig } from 'conf/domain/model/ConfirmationReceipt';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

export const apiConfirmationReceipt = {
  get: async (dataflowId, dataProviderId) => {
    const tokens = userStorage.get();

    const response = await HTTPRequester.download({
      url: getUrl(ConfirmationReceiptConfig.get, {
        dataflowId,
        dataProviderId
      }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });

    return response.data;
    /*  const response = {
      idDataflow: 5061,
      dataflowName: 'DF name that is very descriptive and large',
      datasets: [
        {
          id: 5833,         
          dataSetName: 'First dataset',
          creationDate: 1581673921830,
          isReleased: true,
          dateReleased: 1581622608687,
          dataProviderId: 14,
          datasetSchema: '5e4668574d625428a08b2468',
          nameDatasetSchema: 's1'
        },
        {
          id: 5834,
          dataSetName: 'Second dataset',
          creationDate: 1581673921826,
          isReleased: true,
          dateReleased: 1581622608687,
          dataProviderId: 14,
          datasetSchema: '5e46685c4d625428a08b2469',
          nameDatasetSchema: 's2'
        }
      ],
      providerEmail: 'vicenteprovider@reportnet.net',
      providerAssignation: 'Denmark'
    };
    return response; */
  }
};
