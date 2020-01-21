import { isNil } from 'lodash';

const USER_TOKEN = 'reportnet_03';

const get = () => {
  const tokens = JSON.parse(sessionStorage.getItem(USER_TOKEN));

  if (isNil(tokens)) {
    return;
  }
  return tokens;
};

const set = tokens => {
  sessionStorage.setItem(USER_TOKEN, JSON.stringify(tokens));
};

const remove = () => sessionStorage.removeItem(USER_TOKEN);

const hasToken = () => {
  const tokens = get();

  return !isNil(tokens);
};

export const LocalUserStorage = {
  get,
  set,
  remove,
  hasToken
};
