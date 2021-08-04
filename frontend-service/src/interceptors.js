import axios from 'axios';

import { UserConfig } from 'repositories/config/model/User';
import { getUrl } from 'repositories/_utils/UrlUtils';
import { HTTPRequester } from 'repositories/_utils/HTTPRequester';

import { userStorage } from 'entities/User/UserStorage';

axios.interceptors.request.use(
  config => {
    const tokens = userStorage.getTokens();
    if (tokens) {
      config.headers['Authorization'] = 'Bearer ' + tokens.accessToken;
    }
    // config.headers['Content-Type'] = 'application/json';
    return config;
  },
  error => {
    Promise.reject(error);
  }
);

axios.interceptors.response.use(
  response => {
    return response;
  },
  error => {
    const originalRequest = error.config;

    if (error?.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      const { refreshToken } = userStorage.getTokens();

      return HTTPRequester.post({
        url: getUrl(UserConfig.refreshToken, { refreshToken })
      }).then(res => {
        const { accessToken, refreshToken } = res.data;

        if (res.status >= 200 && res.status <= 299) {
          userStorage.setPropertyToSessionStorage({ accessToken, refreshToken });
          axios.defaults.headers.common['Authorization'] = 'Bearer ' + userStorage.getTokens().accessToken;

          return axios(originalRequest);
        }
      });
    }

    if (error?.response?.status === 403) {
      window.location.href = '/dataflows/error/notAllowed';
      return;
    }

    // return Error object with Promise
    return Promise.reject(error);
  }
);
