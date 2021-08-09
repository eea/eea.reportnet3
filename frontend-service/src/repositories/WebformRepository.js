import { WebformConfig } from './config/WebformConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const WebformRepository = {
  addPamsRecords: async (datasetId, pamsRecord) =>
    await HTTPRequester.post({ url: getUrl(WebformConfig.createPamsRecords, { datasetId }), data: pamsRecord }),

  singlePamData: async (datasetId, groupPaMId) =>
    await HTTPRequester.get({ url: getUrl(WebformConfig.singlePamData, { datasetId, groupPaMId }) })
};
