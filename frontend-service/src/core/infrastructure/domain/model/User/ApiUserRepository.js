import jwt_decode from 'jwt-decode';
import moment from 'moment';

import { api } from 'core/infrastructure/api';
import { User } from 'core/domain/model/User/User';
import { userStorage } from 'core/domain/model/User/UserStorage';

const timeOut = time => {
  setTimeout(() => {
    refreshToken();
  }, time);
};

const login = async (userName, password) => {
  const userTokensTDO = await api.login(userName, password);
  const userTDO = jwt_decode(userTokensTDO.accessToken);
  const user = new User(
    userTDO.sub,
    userTDO.name,
    userTDO.resource_access.account.roles,
    userTDO.preferred_username,
    userTDO.exp
  );
  userStorage.set(userTokensTDO);
  //calculate difference between now and expiration
  const remain = userTDO.exp - moment().unix();
  timeOut((remain - 10) * 1000);
  return user;
};
const logout = async userId => {
  userStorage.remove();
  return;
};
const refreshToken = async refreshToken => {
  const currentTokens = userStorage.get();
  const userTokensTDO = await api.refreshToken(currentTokens.refreshToken);
  const userTDO = jwt_decode(userTokensTDO.accessToken);
  const user = new User(
    userTDO.sub,
    userTDO.name,
    userTDO.resource_access.account.roles,
    userTDO.preferred_username,
    userTDO.exp
  );
  const remain = userTDO.exp - moment().unix();
  timeOut((remain - 10) * 1000);
  userStorage.set(userTokensTDO);
  return;
};

export const ApiUserRepository = {
  login,
  logout,
  refreshToken
};
