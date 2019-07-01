import axios from 'axios';
import config from '../../conf/web.config.json';

const HTTPRequester = (function () {

  const baseURL = `${config.api.protocol}${config.api.url}${config.api.port}`;
  //TODO: Existe axios-hook para usar hooks. Posible mejora.
  //Maps object queryString to 'key=value' format. Checks if queryString is undefined or empty object
  const mapQueryString = (queryString) => (!queryString) ? "" : (Object.entries(
      queryString).length <= 0) ? "" : `?${Object.entries(queryString).map(
      key => `${key[0]}=${key[1]}&`).join("").slice(0, -1)}`;

  const HTTPRequesterAPI = {
    /* AXIOS */
    get: (options) => {
      return axios.get(`${baseURL}${options.url}${mapQueryString(options.queryString)}`);
    },
    post: (options) => {
      return axios.post(`${baseURL}${options.url}`, options.data);
    },
    update: (options) => {
      return axios.put(`${baseURL}${options.url}`, options.data);
    },
    delete: (options) => {
      return axios.delete(`${baseURL}${options.url}`, options.data);
    }
  }
  return HTTPRequesterAPI
})();

export default HTTPRequester;