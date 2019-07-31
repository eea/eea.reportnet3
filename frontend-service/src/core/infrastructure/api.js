import { config } from 'assets/conf';

import { HTTPRequester } from './HTTPRequester';

export const api = {
  dataflows: async () => {
    const response = await HTTPRequester.get('/characters.json');
    return response.json();
  },
  documents: async () => {
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON ? '/jsons/list-of-documents.json' : `${config.loadDatasetsByDataflowID.url}`,
      queryString: {}
    });
    return response.data.documents;
  },
  snapshots: async () => {
    const response = await HTTPRequester.get({
      url: '/jsons/snapshots.json',
      queryString: {}
    });
    return response.data;
  },
  webLinks: async () => {
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON ? '/jsons/list-of-documents.json' : `${config.loadDatasetsByDataflowID.url}`,
      queryString: {}
    });
    return response.data.weblinks;
  }
};
