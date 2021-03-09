import dayjs from 'dayjs';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf/index';

import { apiUser } from 'core/infrastructure/api/domain/model/User';
import { User } from 'core/domain/model/User/User';
import { userStorage } from 'core/domain/model/User/UserStorage';

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
  userStorage.setPropertyToSessionStorage({ accessToken, refreshToken });
  const userInfoDTO = await apiUser.userInfo(userDTO.userId);
  user.email = userInfoDTO.data.email;
  user.firstName = userInfoDTO.data.firstName;
  user.lastName = userInfoDTO.data.lastName;
  //calculate difference between now and expiration
  const remain = userDTO.accessTokenExpiration - dayjs().unix();
  timeOut((remain - 10) * 1000);
  return user;
};

const logout = async () => {
  const currentTokens = userStorage.getTokens();
  userStorage.remove();
  if (currentTokens) {
    const response = await apiUser.logout(currentTokens.refreshToken);
    return response;
  }
  return;
};

const uploadImg = async (userId, imgData) => {
  const response = await apiUser.uploadImg(userId, imgData);
  return response;
};

const userInfo = async userId => {
  const userDTO = await apiUser.userInfo(userId);
  const user = new User({
    email: userDTO.email,
    firstName: userDTO.firstName,
    lastName: userDTO.lastName
  });

  return user;
};

const getConfiguration = async () => {
  const userConfigurationDTO = await apiUser.configuration();
  return parseConfigurationDTO(userConfigurationDTO);
};

const parseConfigurationDTO = userConfigurationDTO => {
  const userConfiguration = {};

  const userDefaultConfiguration = {
    amPm24h: true,
    basemapLayer: 'Topographic',
    dateFormat: 'YYYY-MM-DD',
    listView: true,
    notificationSound: false,
    pinnedDataflows: [],
    rowsPerPage: 10,
    showLogoutConfirmation: true,
    userImage: [],
    visualTheme: 'light'
  };
  if (isNil(userConfigurationDTO) || isEmpty(userConfigurationDTO)) {
    userConfiguration.basemapLayer = userDefaultConfiguration.basemapLayer;
    userConfiguration.dateFormat = userDefaultConfiguration.dateFormat;
    userConfiguration.notificationSound = userDefaultConfiguration.notificationSound;
    userConfiguration.showLogoutConfirmation = userDefaultConfiguration.showLogoutConfirmation;
    userConfiguration.rowsPerPage = userDefaultConfiguration.rowsPerPage;
    userConfiguration.visualTheme = userDefaultConfiguration.visualTheme;
    userConfiguration.userImage = userDefaultConfiguration.userImage;
    userConfiguration.amPm24h = userDefaultConfiguration.amPm24h;
    userConfiguration.listView = userDefaultConfiguration.listView;
    userConfiguration.pinnedDataflows = userDefaultConfiguration.pinnedDataflows;
  } else {
    userConfiguration.basemapLayer = !isNil(userConfigurationDTO.basemapLayer)
      ? userConfigurationDTO.basemapLayer[0]
      : userDefaultConfiguration.basemapLayer;

    userConfiguration.pinnedDataflows = !isNil(userConfigurationDTO.pinnedDataflows)
      ? userConfigurationDTO.pinnedDataflows
      : userDefaultConfiguration.pinnedDataflows;

    userConfiguration.dateFormat = !isNil(userConfigurationDTO.dateFormat[0])
      ? userConfigurationDTO.dateFormat[0]
      : userDefaultConfiguration.dateFormat;

    userConfiguration.notificationSound = isNil(userConfigurationDTO.notificationSound)
      ? userDefaultConfiguration.notificationSound
      : userConfigurationDTO.notificationSound[0] === 'false'
      ? (userConfiguration.notificationSound = false)
      : (userConfiguration.notificationSound = true);

    userConfiguration.showLogoutConfirmation = isNil(userConfigurationDTO.showLogoutConfirmation)
      ? userDefaultConfiguration.showLogoutConfirmation
      : userConfigurationDTO.showLogoutConfirmation[0] === 'false'
      ? (userConfiguration.showLogoutConfirmation = false)
      : (userConfiguration.showLogoutConfirmation = true);

    userConfiguration.rowsPerPage = !isNil(userConfigurationDTO.rowsPerPage)
      ? parseInt(userConfigurationDTO.rowsPerPage[0])
      : userDefaultConfiguration.rowsPerPage;

    userConfiguration.visualTheme = !isNil(userConfigurationDTO.visualTheme)
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
  userStorage.setPropertyToSessionStorage({ accessToken, refreshToken });
  const userInfoDTO = await apiUser.userInfo(userDTO.userId);
  user.email = userInfoDTO.data.email;
  user.firstName = userInfoDTO.data.firstName;
  user.lastName = userInfoDTO.data.lastName;
  //calculate difference between now and expiration
  const remain = userDTO.accessTokenExpiration - dayjs().unix();
  timeOut((remain - 10) * 1000);
  return user;
};

const refreshToken = async () => {
  try {
    const currentTokens = userStorage.getTokens();
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
    userStorage.setPropertyToSessionStorage({ accessToken, refreshToken });
    const userInfoDTO = await apiUser.userInfo(userDTO.userId);
    user.email = userInfoDTO.data.email;
    user.firstName = userInfoDTO.data.firstName;
    user.lastName = userInfoDTO.data.lastName;
    //calculate difference between now and expiration
    const remain = userDTO.accessTokenExpiration - dayjs().unix();
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
  return userStorage.getTokens().accessToken;
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
  uploadImg,
  userInfo
};
