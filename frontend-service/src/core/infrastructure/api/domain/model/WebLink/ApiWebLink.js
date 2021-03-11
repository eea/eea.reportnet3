import { DataflowConfig } from 'conf/domain/model/Dataflow';
import { WebLinkConfig } from 'conf/domain/model/WebLink';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiWebLink = {
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
