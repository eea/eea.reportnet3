import { DataCollectionConfig } from 'conf/domain/model/DataCollection';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

const apiDataCollection = {
  create: async (dataflowId, endDate, isManualTechnicalAcceptance, stopAndNotifySQLErrors, showPublicInfo) => {
    const response = await HTTPRequester.post({
      url: getUrl(DataCollectionConfig.createDataCollection, {
        isManualTechnicalAcceptance,
        stopAndNotifySQLErrors,
        showPublicInfo
      }),

      data: {
        idDataflow: dataflowId,
        dueDate: endDate
      }
    });
    return response;
  },

  update: async dataflowId => {
    const response = await HTTPRequester.update({
      url: getUrl(DataCollectionConfig.updateDataCollectionNewRepresentatives, { dataflowId })
    });
    return response;
  }
};
export { apiDataCollection };
