import { WebLinkConfig } from './config/WebLinkConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const WebLinkRepository = {
  getAll: async dataflowId => await HTTPRequester.get({ url: getUrl(WebLinkConfig.getAll, { dataflowId }) }),

  create: async (dataflowId, webLink) =>
    await HTTPRequester.post({
      url: getUrl(WebLinkConfig.create, { dataflowId }),
      data: { description: webLink.description, url: webLink.url.toString(), isPublic: webLink.isPublic }
    }),

  delete: async (webLinkId, dataflowId) => await HTTPRequester.delete({ url: getUrl(WebLinkConfig.delete, { webLinkId, dataflowId }) }),

  update: async (dataflowId, webLinkToEdit) =>
    await HTTPRequester.update({
      url: getUrl(WebLinkConfig.update, { dataflowId }),
      data: { description: webLinkToEdit.description, id: webLinkToEdit.id, isPublic: webLinkToEdit.isPublic, url: webLinkToEdit.url  }
    })
};
