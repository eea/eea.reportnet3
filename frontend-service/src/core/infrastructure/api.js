import { config } from 'assets/conf';

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
  documentById: async url => {
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON ? '' : url,
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
