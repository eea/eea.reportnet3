import axios from 'axios';
import isNil from 'lodash/isNil';

import { UserConfig } from 'repositories/config/UserConfig';
import { getUrl } from 'repositories/_utils/UrlUtils';
import { HTTPRequester } from 'repositories/_utils/HTTPRequester';

import { LocalUserStorageUtils } from 'services/_utils/LocalUserStorageUtils';
import { showSessionExpiredDialog } from 'services/_utils/SessionDialogUtils';

axios.interceptors.request.use(
  config => {
    const tokens = LocalUserStorageUtils.getTokens();
    if (!isNil(tokens)) {
      config.headers['Authorization'] = 'Bearer ' + tokens.accessToken;
    }
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

    // Handle 401 Unauthorized
    if (error?.response?.status === 401) {

      if (originalRequest.url && originalRequest.url.includes('/refreshToken')) {
        console.error('Error during token refresh:', error.response?.data);
        showSessionExpiredDialog();
        return Promise.reject(error);
      }
      if (!originalRequest._retry) {
        originalRequest._retry = true;
        const tokens = LocalUserStorageUtils.getTokens();

        if (isNil(tokens)) {
          return Promise.reject(error);
        }

        const { refreshToken } = tokens;

        return HTTPRequester.post({
          url: getUrl(UserConfig.refreshToken, { refreshToken })
        })
          .then(res => {
            const { accessToken, refreshToken } = res.data;

            if (res.status >= 200 && res.status <= 299) {
              LocalUserStorageUtils.setPropertyToSessionStorage({ accessToken, refreshToken });
              axios.defaults.headers.common['Authorization'] =
                'Bearer ' + LocalUserStorageUtils.getTokens().accessToken;

              return axios(originalRequest);
            }
          })
          .catch(refreshError => {
            if (originalRequest.url.includes('/refreshToken')) {
              console.error('Error during token refresh:', refreshError.response?.data);
              showSessionExpiredDialog();
            }
            return Promise.reject(refreshError);
          });
      }
    }

    if (error?.response?.status === 403) {
      window.location.href = '/dataflows/error/notAllowed';
      return;
    }

    // Handle 500 from refreshToken endpoint
    if (error?.response?.status === 500 && originalRequest.url && originalRequest.url.includes('/refreshToken')) {
      showSessionExpiredDialog();
      return Promise.reject(error);
    }
    // return Error object with Promise
    return Promise.reject(error);
  }
);
