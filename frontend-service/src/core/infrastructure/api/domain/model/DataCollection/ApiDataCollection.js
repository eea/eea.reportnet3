import { DataCollectionConfig } from 'conf/domain/model/DataCollection';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

const apiDataCollection = {
  create: async (dataflowId, endDate, isManualTechnicalAcceptance, stopAndNotifySQLErrors, showPublicInfo) =>
    await HTTPRequester.post({
      url: getUrl(DataCollectionConfig.createDataCollection, {
        isManualTechnicalAcceptance,
        stopAndNotifySQLErrors,
        showPublicInfo
      }),

      data: {
        idDataflow: dataflowId,
        dueDate: endDate
      }
    }),

  createReference: async (dataflowId, stopAndNotifyPKError) => {
    return await HTTPRequester.post({
      url: getUrl(DataCollectionConfig.createReference, { stopAndNotifyPKError }),
      data: { idDataflow: dataflowId }
    });
  },

  update: async dataflowId =>
    await HTTPRequester.update({
      url: getUrl(DataCollectionConfig.updateDataCollectionNewRepresentatives, { dataflowId })
    })
};
export { apiDataCollection };
