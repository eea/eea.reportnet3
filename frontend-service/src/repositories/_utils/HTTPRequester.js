import axios from 'axios';

export const HTTPRequester = (() => {
  const baseURL = window.env.REACT_APP_BACKEND;
  //Maps object queryString to 'key=value' format. Checks if queryString is undefined or empty object
  const mapQueryString = queryString =>
    !queryString
      ? ''
      : Object.entries(queryString).length <= 0
      ? ''
      : `?${Object.entries(queryString)
          .map(key => `${key[0]}=${key[1]}&`)
          .join('')
          .slice(0, -1)}`;

  const HTTPRequesterAPI = {
    /* AXIOS */
    get: options => {
      const headers = options.headers;
      return axios.get(`${baseURL}${options.url}${mapQueryString(options.queryString)}`, { headers });
    },
    download: options => {
      const headers = options.headers;
      return axios.get(`${baseURL}${options.url}${mapQueryString(options.queryString)}`, {
        responseType: 'blob',
        headers
      });
    },
    post: options => {
      const headers = options.headers;
      return axios.post(`${baseURL}${options.url}`, options.data, { headers });
    },
    update: options => {
      const headers = options.headers;
      return axios.put(`${baseURL}${options.url}`, options.data, { headers });
    },
    delete: options => {
      const data = options.data;
      const headers = options.headers;
      return axios.delete(`${baseURL}${options.url}`, { data }, { headers });
    },
    postWithFiles: options => {
      const headers = options.headers;
      return axios.post(`${baseURL}${options.url}`, options.data, { headers });
    },
    putWithFiles: options => {
      const headers = options.headers;
      return axios.put(`${baseURL}${options.url}`, options.data, { headers });
    }
  };
  return HTTPRequesterAPI;
})();
