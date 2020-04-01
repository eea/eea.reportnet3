import { DataCollectionConfig } from 'conf/domain/model/DataCollection';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

const apiDataCollection = {
  create: async (dataflowId, endDate) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.post({
      url: getUrl(DataCollectionConfig.createDataCollection),
      data: {
        idDataflow: dataflowId,
        dueDate: endDate
      },
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response;
  },

  update: async dataflowId => {
    const tokens = userStorage.get();

    const response = await HTTPRequester.update({
      url: getUrl(DataCollectionConfig.updateDataCollectionNewRepresentatives, { dataflowId }),

      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response;
  }
};
export { apiDataCollection };
