import { config } from 'assets/conf';
import { getUrl } from 'core/infrastructure/getUrl';
import { HTTPRequester } from './HTTPRequester';

export const api = {
  dataflows: async () => {
    const response = await HTTPRequester.get('/characters.json');
    return response.json();
  },

  documents: async url => {
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON ? '/jsons/list-of-documents.json' : url,
      queryString: {}
    });
    return response.data.documents;
  },

  downloadDocumentById: async documentId => {
    const response = await HTTPRequester.get({
      url: getUrl(config.downloadDocumentByIdAPI.url, {
        documentId: documentId
      }),
      queryString: {}
    });
    return response.data;
  },

  snapshots: async url => {
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON ? '/jsons/snapshots.json' : url,
      queryString: {}
    });
    return response.data;
  },

  webLinks: async url => {
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON ? '/jsons/list-of-documents.json' : url,
      queryString: {}
    });
    return response.data.weblinks;
  }
};
