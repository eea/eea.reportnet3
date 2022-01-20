import { WebformConfig } from './config/WebformConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const WebformRepository = {
  addPamsRecords: async (datasetId, pamsRecord) =>
    await HTTPRequester.post({ url: getUrl(WebformConfig.createPamsRecords, { datasetId }), data: pamsRecord }),

  create: async (name, jsonFile) => {
    return await HTTPRequester.post({
      url: getUrl(WebformConfig.create),
      data: { name, content: jsonFile }
    });
  },

  delete: async id => await HTTPRequester.delete({ url: getUrl(WebformConfig.delete, { id }) }),

  download: async id => await HTTPRequester.download({ url: getUrl(WebformConfig.download, { id }) }),

  getAll: async () => {
    const { data } = await HTTPRequester.get({ url: getUrl(WebformConfig.getAll) });
    return data;
  },

  getSinglePamData: async (datasetId, groupPaMId) =>
    await HTTPRequester.get({ url: getUrl(WebformConfig.getSinglePamData, { datasetId, groupPaMId }) }),

  getWebformConfig: async webformId => {
    const { data } = await HTTPRequester.get({ url: getUrl(WebformConfig.getWebformConfig, { webformId }) });
    return data;
  },

  update: async (name, jsonFile, id) => {
    const data = {};
    if (jsonFile) {
      data.content = jsonFile;
    }

    if (name) {
      data.name = name;
    }

    return await HTTPRequester.update({
      url: getUrl(WebformConfig.update, { id }),
      data
    });
  }
};
