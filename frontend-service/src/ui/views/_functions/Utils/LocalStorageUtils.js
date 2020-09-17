import isNil from 'lodash/isNil';

const LOCAL_KEY = 'Reportnet_03';

const get = () => {
  const rnLocalStorage = JSON.parse(localStorage.getItem(LOCAL_KEY));
  if (!isNil(rnLocalStorage)) return rnLocalStorage;
  return;
};

const remove = () => {
  localStorage.removeItem(LOCAL_KEY);
};

const set = value => {
  localStorage.setItem(LOCAL_KEY, JSON.stringify(value));
};

export const LocalStorageUtils = {
  get,
  remove,
  set
};
