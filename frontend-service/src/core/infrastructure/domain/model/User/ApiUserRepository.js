import jwt_decode from 'jwt-decode';
import moment from 'moment';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

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

const getConfiguration = async () => {
  const userConfigurationDTO = await apiUser.configuration();
  return parseConfigurationDTO(userConfigurationDTO);
};

const parseConfigurationDTO = userConfigurationDTO => {
  const userConfiguration = {};

  const userDefaultConfiguration = {
    dateFormat: 'YYYY-MM-DD',
    showLogoutConfirmation: true,
    rowsPerPage: 10,
    visualTheme: 'light',
    userImage: [],
    amPm24h: true,
    listView: true
  };

  if (isNil(userConfigurationDTO) || isEmpty(userConfigurationDTO)) {
    userConfiguration.dateFormat = userDefaultConfiguration.dateFormat;
    userConfiguration.showLogoutConfirmation = userDefaultConfiguration.showLogoutConfirmation;
    userConfiguration.rowsPerPage = userDefaultConfiguration.rowsPerPage;
    userConfiguration.visualTheme = userDefaultConfiguration.visualTheme;
    userConfiguration.userImage = userDefaultConfiguration.userImage;
    userConfiguration.amPm24h = userDefaultConfiguration.amPm24h;
    userConfiguration.listView = userDefaultConfiguration.listView;
  } else {
    userConfiguration.dateFormat = !isNil(userConfigurationDTO.dateFormat[0])
      ? userConfigurationDTO.dateFormat[0]
      : userDefaultConfiguration.dateFormat;

    userConfiguration.showLogoutConfirmation = isNil(userConfigurationDTO.showLogoutConfirmation)
      ? userDefaultConfiguration.showLogoutConfirmation
      : userConfigurationDTO.showLogoutConfirmation[0] === 'false'
      ? (userConfiguration.showLogoutConfirmation = false)
      : (userConfiguration.showLogoutConfirmation = true);

    userConfiguration.rowsPerPage = !isNil(userConfigurationDTO.rowsPerPage[0])
      ? parseInt(userConfigurationDTO.rowsPerPage[0])
      : userDefaultConfiguration.rowsPerPage;

    userConfiguration.visualTheme = !isNil(userConfigurationDTO.visualTheme[0])
      ? userConfigurationDTO.visualTheme[0]
      : userDefaultConfiguration.visualTheme;

    userConfiguration.userImage = !isNil(userConfigurationDTO.userImage)
      ? userConfigurationDTO.userImage
      : userDefaultConfiguration.userImage;

    userConfiguration.amPm24h = isNil(userConfigurationDTO.amPm24h)
      ? userDefaultConfiguration.amPm24h
      : userConfigurationDTO.amPm24h[0] === 'false'
      ? (userConfiguration.amPm24h = false)
      : (userConfiguration.amPm24h = true);

    userConfiguration.listView = isNil(userConfigurationDTO.listView)
      ? userDefaultConfiguration.listView
      : userConfigurationDTO.listView[0] === 'false'
      ? (userConfiguration.listView = false)
      : (userConfiguration.listView = true);
  }
  return userConfiguration;
};

const updateAttributes = async attributes => await apiUser.updateAttributes(attributes);

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
  getConfiguration,
  updateAttributes,
  logout,
  oldLogin,
  refreshToken,
  getToken,
  userRole,
  uploadImg
};
