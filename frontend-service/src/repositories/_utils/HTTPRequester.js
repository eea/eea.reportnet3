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
    get: options =>
      axios.get(`${baseURL}${options.url}${mapQueryString(options.queryString)}`, { headers: options.headers }),

    download: options =>
      axios.get(`${baseURL}${options.url}${mapQueryString(options.queryString)}`, {
        responseType: 'blob',
        headers: options.headers
      }),

    post: options => axios.post(`${baseURL}${options.url}`, options.data, { headers: options.headers }),

    update: options => axios.put(`${baseURL}${options.url}`, options.data, { headers: options.headers }),

    delete: options => axios.delete(`${baseURL}${options.url}`, options.data, { headers: options.headers }),

    postWithFiles: options => axios.post(`${baseURL}${options.url}`, options.data, { headers: options.headers }),

    putWithFiles: options => axios.put(`${baseURL}${options.url}`, options.data, { headers: options.headers })
  };

  return HTTPRequesterAPI;
})();
