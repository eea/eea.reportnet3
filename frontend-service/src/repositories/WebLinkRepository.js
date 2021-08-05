import { DataflowConfig } from './config/DataflowConfig';
import { WebLinkConfig } from './config/WebLinkConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const webLinkRepository = {
  all: async dataflowId => {
    return await HTTPRequester.get({ url: getUrl(DataflowConfig.loadDatasetsByDataflowId, { dataflowId }) });
  },

  create: async (dataflowId, webLink) => {
    return await HTTPRequester.post({
      url: getUrl(WebLinkConfig.create, { dataflowId }),
      data: { description: webLink.description, url: webLink.url.toString() }
    });
  },

  deleteWebLink: async webLinkToDelete => {
    return await HTTPRequester.delete({ url: getUrl(WebLinkConfig.delete, { weblinkId: webLinkToDelete.id }) });
  },

  update: async (dataflowId, webLinkToEdit) => {
    return await HTTPRequester.update({
      url: getUrl(WebLinkConfig.update, { dataflowId }),
      data: { description: webLinkToEdit.description, id: webLinkToEdit.id, url: webLinkToEdit.url }
    });
  }
};
