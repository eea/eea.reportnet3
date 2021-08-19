import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';

const { storage: storageConfig } = config;

const getSessionStorage = () => {
  const cLocalStorage = JSON.parse(sessionStorage.getItem(storageConfig.LOCAL_KEY));
  if (!isNil(cLocalStorage)) return cLocalStorage;
  return;
};

const removeSessionStorage = () => {
  sessionStorage.clear();
};

const setPropertyToSessionStorage = prop => {
  const cLocalStorage = getSessionStorage();
  sessionStorage.setItem(storageConfig.LOCAL_KEY, JSON.stringify({ ...cLocalStorage, ...prop }));
};

const removeSessionStorageProperty = key => {
  const cLocalStorage = getSessionStorage();
  if (!isNil(cLocalStorage)) {
    delete cLocalStorage[key];
    if (!isNil(cLocalStorage) && !isEmpty(cLocalStorage)) {
      setSessionStorage(cLocalStorage);
    } else {
      sessionStorage.removeItem(storageConfig.LOCAL_KEY);
    }
  }
};

const getPropertyFromSessionStorage = key => {
  const cLocalStorage = getSessionStorage();
  if (cLocalStorage) return cLocalStorage[key];
  return;
};

const setSessionStorage = value => {
  sessionStorage.setItem(storageConfig.LOCAL_KEY, JSON.stringify(value));
};

const getTokens = () => {
  const cSessionStorage = getSessionStorage();
  if (!isNil(cSessionStorage)) {
    const { accessToken, refreshToken } = cSessionStorage;
    if (accessToken && refreshToken) {
      return { accessToken, refreshToken };
    }
    return;
  }
  return;
};

const set = tokens => {
  sessionStorage.setItem(storageConfig.LOCAL_KEY, JSON.stringify(tokens));
};

const remove = () => sessionStorage.removeItem(storageConfig.LOCAL_KEY);

const hasToken = () => {
  const tokens = getTokens();

  return !isNil(tokens);
};

export const LocalUserStorageUtils = {
  getTokens,
  getSessionStorage,
  getPropertyFromSessionStorage,
  hasToken,
  remove,
  removeSessionStorageProperty,
  removeSessionStorage,
  set,
  setSessionStorage,
  setPropertyToSessionStorage
};
