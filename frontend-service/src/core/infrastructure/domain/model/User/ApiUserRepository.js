import jwt_decode from 'jwt-decode';
import moment from 'moment';

import { apiUser } from 'core/infrastructure/api/domain/model/User';
import { User } from 'core/domain/model/User/User';
import { userStorage } from 'core/domain/model/User/UserStorage';
import { config } from 'conf/index';

const timeOut = time => {
  setTimeout(() => {
    refreshToken();
  }, time);
};

const login = async code => {
  const userTokensTDO = await apiUser.login(code);
  const userTDO = jwt_decode(userTokensTDO.accessToken);
  const user = new User(userTDO.sub, userTDO.name, userTDO.user_groups, userTDO.preferred_username, userTDO.exp);
  userStorage.set(userTokensTDO);
  //calculate difference between now and expiration
  const remain = userTDO.exp - moment().unix();
  timeOut((remain - 10) * 1000);
  return user;
};
const logout = async userId => {
  const currentTokens = userStorage.get();
  userStorage.remove();
  const response = await apiUser.logout(currentTokens.refreshToken);
  return response;
};
const refreshToken = async refreshToken => {
  try {
    const currentTokens = userStorage.get();
    const userTokensDTO = await apiUser.refreshToken(currentTokens.refreshToken);
    const userDTO = jwt_decode(userTokensDTO.accessToken);
    const user = new User(userDTO.sub, userDTO.name, userDTO.user_groups, userDTO.preferred_username, userDTO.exp);
    const remain = userDTO.exp - moment().unix();
    timeOut((remain - 10) * 1000);
    userStorage.set(userTokensDTO);
    return user;
  } catch (error) {
    userStorage.remove();
  }
};

const hasPermission = (user, permissions, entity) => {
  let allow = false;
  permissions.forEach(permision => {
    const role = `${entity}-${permision}`;
    if (user.roles.includes(role)) allow = true;
  });
  return allow;
};
const userRole = (user, entity) => {
  const roleDTO = user.roles.filter(role => role.includes(entity));
  if (roleDTO.length) {
    const [roleName] = roleDTO[0].split('-').reverse();
    return config.permissions[roleName];
  }
  return;
};

export const ApiUserRepository = {
  login,
  logout,
  refreshToken,
  hasPermission,
  userRole
};
