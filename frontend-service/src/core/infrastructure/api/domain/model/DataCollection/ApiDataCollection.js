import { DataCollectionConfig } from 'conf/domain/model/DataCollection';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

export const apiDataCollection = {
  create: async (dataflowId, schemaId, dataCollectionName, creationDate) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.post({
      url: getUrl(DataCollectionConfig.createDataCollection),
      data: {
        dataSetName: dataCollectionName,
        datasetSchema: schemaId,
        dueDate: creationDate,
        idDataflow: dataflowId
      },
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response;
  }
};
