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
  const userDTO = await apiUser.login(code);
  const { accessToken, refreshToken } = userDTO;
  const user = new User({
    accessRole: userDTO.roles,
    contextRoles: userDTO.groups,
    id: userDTO.userId,
    name: userDTO.preferredUsername,
    preferredUsername: userDTO.preferredUsername,
    tokenExpireTime: userDTO.accessTokenExpiration
  });
  userStorage.set({ accessToken, refreshToken });
  //calculate difference between now and expiration
  const remain = userDTO.accessTokenExpiration - moment().unix();
  timeOut((remain - 10) * 1000);
  return user;
};

const logout = async () => {
  const currentTokens = userStorage.get();
  userStorage.remove();
  const response = await apiUser.logout(currentTokens.refreshToken);
  return response;
};

const uploadImg = async (userId, imgData) => {
  const response = await apiUser.uploadImg(userId, imgData);
  return response;
};

const userData = async userId => {
  const response = await apiUser.userData(userId);
  //const response = new Promise((resolve, reject) => { dato: 'hola' });
  return response;
};
const updateAttributes = async Attributes => {
  const response = await apiUser.updateAttributes(Attributes);
  return response;
};
const oldLogin = async (userName, password) => {
  const userDTO = await apiUser.oldLogin(userName, password);
  const { accessToken, refreshToken } = userDTO;
  const user = new User({
    accessRole: userDTO.roles,
    contextRoles: userDTO.groups,
    id: userDTO.userId,
    name: userDTO.preferredUsername,
    preferredUsername: userDTO.preferredUsername,
    tokenExpireTime: userDTO.accessTokenExpiration
  });
  userStorage.set({ accessToken, refreshToken });
  //calculate difference between now and expiration
  const remain = userDTO.accessTokenExpiration - moment().unix();
  timeOut((remain - 10) * 1000);
  return user;
};

const refreshToken = async () => {
  try {
    const currentTokens = userStorage.get();
    const userDTO = await apiUser.refreshToken(currentTokens.refreshToken);
    const { accessToken, refreshToken } = userDTO;
    const user = new User({
      accessRole: userDTO.roles,
      contextRoles: userDTO.groups,
      id: userDTO.userId,
      name: userDTO.preferredUsername,
      preferredUsername: userDTO.preferredUsername,
      tokenExpireTime: userDTO.accessTokenExpiration
    });
    userStorage.set({ accessToken, refreshToken });
    //calculate difference between now and expiration
    const remain = userDTO.accessTokenExpiration - moment().unix();
    timeOut((remain - 10) * 1000);
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
  userData,
  updateAttributes,
  logout,
  oldLogin,
  refreshToken,
  hasPermission,
  getToken,
  userRole,
  uploadImg
};
