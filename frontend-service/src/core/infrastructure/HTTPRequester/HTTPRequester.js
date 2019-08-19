import axios from 'axios';

export const HTTPRequester = (function() {
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
      return axios.get(`${baseURL}${options.url}${mapQueryString(options.queryString)}`);
    },
    download: options => {
      return axios.get(`${baseURL}${options.url}${mapQueryString(options.queryString)}`, {
        responseType: 'blob',
        headers: { 'Content-Type': 'application/octet-stream' }
      });
    },
    post: options => {
      return axios.post(`${baseURL}${options.url}`, options.data);
    },
    update: options => {
      return axios.put(`${baseURL}${options.url}`, options.data);
    },
    delete: options => {
      return axios.delete(`${baseURL}${options.url}`, options.data);
    },
    postWithFiles: options => {
      return axios.post(`${baseURL}${options.url}`, options.data, {
        headers: { 'Content-Type': undefined }
      });
    }
  };
  return HTTPRequesterAPI;
})();
