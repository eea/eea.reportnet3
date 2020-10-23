import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';

const { storage: storageConfig } = config;

const getLocalStorage = () => {
  const cLocalStorage = JSON.parse(localStorage.getItem(storageConfig.LOCAL_KEY));
  if (!isNil(cLocalStorage)) return cLocalStorage;
  return;
};

const removeLocalStorage = () => {
  localStorage.clear();
};

const setPropertyToLocalStorage = prop => {
  const cLocalStorage = getLocalStorage();
  localStorage.setItem(storageConfig.LOCAL_KEY, JSON.stringify({ ...cLocalStorage, ...prop }));
};

const removeLocalProperty = key => {
  const cLocalStorage = getLocalStorage();
  if (!isNil(cLocalStorage)) {
    delete cLocalStorage[key];
    if (!isNil(cLocalStorage) && !isEmpty(cLocalStorage)) {
      setLocalStorage(cLocalStorage);
    } else {
      localStorage.removeItem(storageConfig.LOCAL_KEY);
    }
  }
};

const getPropertyFromLocalStorage = key => {
  const cLocalStorage = getLocalStorage();
  if (cLocalStorage) return cLocalStorage[key];
  return;
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
  getLocalStorage,
  getPropertyFromLocalStorage,
  hasToken,
  remove,
  removeLocalProperty,
  removeLocalStorage,
  set,
  setLocalStorage,
  setPropertyToLocalStorage
};
