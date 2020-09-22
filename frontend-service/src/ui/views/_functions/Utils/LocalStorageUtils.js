import isNil from 'lodash/isNil';

import { config } from 'conf';

const { storage: storageConfig } = config;

const get = () => {
  const rnLocalStorage = JSON.parse(localStorage.getItem(storageConfig.LOCAL_KEY));
  if (!isNil(rnLocalStorage)) return rnLocalStorage;
  return;
};

const remove = () => {
  localStorage.removeItem(storageConfig.LOCAL_KEY);
};

const set = value => {
  localStorage.setItem(storageConfig.LOCAL_KEY, JSON.stringify(value));
};

export const LocalStorageUtils = {
  get,
  remove,
  set
};
