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
    const response = await HTTPRequester.download({
      url: window.env.REACT_APP_JSON
        ? '/jsons/list-of-documents.json'
        : getUrl(config.downloadDocumentByIdAPI.url, {
            documentId: documentId
          }),
      queryString: {}
    });
    return response.data;
  },

  uploadDocument: async (dataFlowId, description, language, file) => {
    const formData = new FormData();
    formData.append('file', file, file.name);
    const response = await HTTPRequester.postWithFiles({
      url: getUrl(config.uploadDocumentAPI.url, {
        dataFlowId: dataFlowId,
        description: description,
        language: language
      }),
      queryString: {},
      data: formData
    });
    return response;
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
