import isNil from 'lodash/isNil';

import { config } from 'conf/index';

const { storage: storageConfig } = config;

const get = () => {
  const rnLocalStorage = JSON.parse(localStorage.getItem(storageConfig.LOCAL_KEY));
  if (!isNil(rnLocalStorage)) return rnLocalStorage;
  return;
};

const remove = () => {
  localStorage.reset();
};

const set = value => {
  localStorage.setItem(storageConfig.LOCAL_KEY, JSON.stringify(value));
};

export const LocalStorageUtils = {
  get,
  remove,
  set
};
