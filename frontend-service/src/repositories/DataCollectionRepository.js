import { DataCollectionConfig } from './config/DataCollectionConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const DataCollectionRepository = {
  create: async (dataflowId, endDate, isManualTechnicalAcceptance, stopAndNotifySQLErrors, showPublicInfo) =>
    await HTTPRequester.post({
      url: getUrl(DataCollectionConfig.create, { isManualTechnicalAcceptance, stopAndNotifySQLErrors, showPublicInfo }),
      data: { idDataflow: dataflowId, dueDate: endDate }
    }),

  createReference: async (dataflowId, stopAndNotifyPKError) =>
    await HTTPRequester.post({
      url: getUrl(DataCollectionConfig.createReference, { stopAndNotifyPKError }),
      data: { idDataflow: dataflowId }
    }),

  update: async dataflowId =>
    await HTTPRequester.update({
      url: getUrl(DataCollectionConfig.update, { dataflowId })
    })
};
