import { User } from 'core/domain/model/User/User';
import { isNil } from 'lodash';

const USER_TOKEN = 'reportnet_03';

const get = () => {
  const tokens = JSON.parse(localStorage.getItem(USER_TOKEN));
  console.log('tokens', tokens);

  if (isNil(tokens)) {
    return;
  }
  return tokens;
};

const set = tokens => {
  localStorage.setItem(USER_TOKEN, JSON.stringify(tokens));
};

const remove = () => localStorage.removeItem(USER_TOKEN);

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
