import { api } from 'core/infrastructure/api';
import { User } from 'core/domain/model/User/User';
import { userStorage } from 'core/domain/model/User/UserStorage';
import jwt_decode from 'jwt-decode';

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
  return user;
};
const logout = async userId => {
  userStorage.remove();
  return;
};
const refreshToken = async refreshToken => {};

export const ApiUserRepository = {
  login,
  logout,
  refreshToken
};
