import { isNil } from 'lodash';

import { config } from 'conf';

const { storage: storageConfig } = config;

const getLocalStorage = () => {
  const rnLocalStorage = JSON.parse(localStorage.getItem(storageConfig.LOCAL_KEY));
  if (!isNil(rnLocalStorage)) return rnLocalStorage;
  return;
};

const removeLocalStorage = () => {
  localStorage.clear();
};

const setLocalStorage = value => {
  localStorage.setItem(storageConfig.LOCAL_KEY, JSON.stringify(value));
};

const get = () => {
  const tokens = JSON.parse(sessionStorage.getItem(storageConfig.LOCAL_KEY));

  if (isNil(tokens)) {
    return;
  }
  return tokens;
};

const set = tokens => {
  sessionStorage.setItem(storageConfig.LOCAL_KEY, JSON.stringify(tokens));
};

const remove = () => sessionStorage.removeItem(storageConfig.LOCAL_KEY);

const hasToken = () => {
  const tokens = get();

  return !isNil(tokens);
};

export const LocalUserStorage = {
  get,
  set,
  remove,
  hasToken,
  getLocalStorage,
  removeLocalStorage,
  setLocalStorage
};
