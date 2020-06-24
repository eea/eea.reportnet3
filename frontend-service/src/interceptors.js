import axios from 'axios';

import { UserConfig } from 'conf/domain/model/User';
import { getUrl } from './core/infrastructure/CoreUtils';

import { userStorage } from 'core/domain/model/User/UserStorage';

import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

axios.interceptors.request.use(
  config => {
    const tokens = userStorage.get();
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
    if (error.response.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      const { refreshToken } = userStorage.get();
      return HTTPRequester.post({
        url: getUrl(UserConfig.refreshToken, { refreshToken })
      }).then(res => {
        console.log({ res });
        const { accessToken, refreshToken } = res.data;
        if (res.status >= 200 && res.status <= 299) {
          userStorage.set({ accessToken, refreshToken });
          axios.defaults.headers.common['Authorization'] = 'Bearer ' + userStorage.get().accessToken;

          return axios(originalRequest);
        }
      });
    }

    // return Error object with Promise
    return Promise.reject(error);
  }
);
