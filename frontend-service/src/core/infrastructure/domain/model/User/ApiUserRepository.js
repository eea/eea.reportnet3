import jwt_decode from 'jwt-decode';
import moment from 'moment';
import { isUndefined } from 'lodash';

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
  const userTokensDTO = await apiUser.login(code);
  const userDTO = jwt_decode(userTokensDTO.accessToken);
  const user = new User(
    userDTO.sub,
    userDTO.name,
    userDTO.realm_access.roles,
    userDTO.user_groups,
    userDTO.preferred_username,
    userDTO.exp
  );
  userStorage.set(userTokensDTO);
  //calculate difference between now and expiration
  const remain = userDTO.exp - moment().unix();
  timeOut((remain - 10) * 1000);
  return user;
};

const logout = async () => {
  const currentTokens = userStorage.get();
  userStorage.remove();
  const response = await apiUser.logout(currentTokens.refreshToken);
  return response;
};
const oldLogin = async (userName, password) => {
  const userTokensDTO = await apiUser.oldLogin(userName, password);
  const userDTO = jwt_decode(userTokensDTO.accessToken);
  const user = new User(
    userDTO.sub,
    userDTO.name,
    userDTO.realm_access.roles,
    userDTO.user_groups,
    userDTO.preferred_username,
    userDTO.exp
  );
  userStorage.set(userTokensDTO);
  //calculate difference between now and expiration
  const remain = userDTO.exp - moment().unix();
  timeOut((remain - 10) * 1000);
  return user;
};
const refreshToken = async refreshToken => {
  try {
    const currentTokens = userStorage.get();
    const userTokensDTO = await apiUser.refreshToken(currentTokens.refreshToken);
    const userDTO = jwt_decode(userTokensDTO.accessToken);
    const user = new User(
      userDTO.sub,
      userDTO.name,
      userDTO.realm_access.roles,
      userDTO.user_groups,
      userDTO.preferred_username,
      userDTO.exp
    );
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
  if (isUndefined(entity)) {
    if (permissions.filter(permission => user.accessRole.includes(permission)).length > 0) allow = true;
  } else {
    permissions.forEach(permission => {
      const role = `${entity}-${permission}`;
      if (user.contextRoles.includes(role)) allow = true;
    });
  }
  return allow;
};

const userRole = (user, entity) => {
  const roleDTO = user.contextRoles.filter(role => role.includes(entity));
  if (roleDTO.length) {
    const [roleName] = roleDTO[0].split('-').reverse();
    return config.permissions[roleName];
  }
  return;
};

const getToken = () => {
  return userStorage.get().accessToken;
};

export const ApiUserRepository = {
  login,
  logout,
  oldLogin,
  refreshToken,
  hasPermission,
  getToken,
  userRole
};
