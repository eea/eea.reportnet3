import dayjs from 'dayjs';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf/index';

import { userRepository } from 'repositories/UserRepository';
import { User } from 'entities/User';
import { LocalUserStorageUtils } from './_utils/LocalUserStorageUtils';

const timeOut = time => {
  setTimeout(() => {
    refreshToken();
  }, time);
};

const login = async code => {
  const userDTO = await userRepository.login(code);
  const { accessToken, refreshToken } = userDTO.data;
  const user = new User({
    accessRole: userDTO.data.roles,
    contextRoles: userDTO.data.groups,
    id: userDTO.data.userId,
    name: userDTO.data.preferredUsername,
    preferredUsername: userDTO.data.preferredUsername,
    tokenExpireTime: userDTO.data.accessTokenExpiration
  });
  LocalUUtils.setPropertyToSessionStorage({ accessToken, refreshToken });
  const userInfoDTO = await userRepository.userInfo(userDTO.data.userId);
  user.email = userInfoDTO.data.email;
  user.firstName = userInfoDTO.data.firstName;
  user.lastName = userInfoDTO.data.lastName;
  //calculate difference between now and expiration
  const remain = userDTO.data.accessTokenExpiration - dayjs().unix();
  timeOut((remain - 10) * 1000);
  return user;
};

const logout = async () => {
  const currentTokens = LocalUUtils.getTokens();
  LocalUUtils.remove();
  if (currentTokens) {
    const response = await userRepository.logout(currentTokens.refreshToken);
    return response;
  }
  return;
};

const userInfo = async userId => {
  const userDTO = await userRepository.userInfo(userId);
  const user = new User({
    email: userDTO.email,
    firstName: userDTO.firstName,
    lastName: userDTO.lastName
  });

  return user;
};

const getConfiguration = async () => {
  const userConfigurationDTO = await userRepository.configuration();
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
    pushNotifications: true,
    rowsPerPage: 10,
    showLogoutConfirmation: true,
    userImage: [],
    visualTheme: 'light'
  };
  if (isNil(userConfigurationDTO) || isEmpty(userConfigurationDTO)) {
    userConfiguration.basemapLayer = userDefaultConfiguration.basemapLayer;
    userConfiguration.dateFormat = userDefaultConfiguration.dateFormat;
    userConfiguration.notificationSound = userDefaultConfiguration.notificationSound;
    userConfiguration.pushNotifications = userDefaultConfiguration.pushNotifications;
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

    userConfiguration.pushNotifications = isNil(userConfigurationDTO.pushNotifications)
      ? userDefaultConfiguration.pushNotifications
      : userConfigurationDTO.pushNotifications[0] === 'false'
      ? (userConfiguration.pushNotifications = false)
      : (userConfiguration.pushNotifications = true);

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

const updateAttributes = async attributes => await userRepository.updateAttributes(attributes);

const oldLogin = async (userName, password) => {
  const userDTO = await userRepository.oldLogin(userName, password);

  const { accessToken, refreshToken } = userDTO.data;
  const user = new User({
    accessRole: userDTO.data.roles,
    contextRoles: userDTO.data.groups,
    id: userDTO.data.userId,
    name: userDTO.data.preferredUsername,
    preferredUsername: userDTO.data.preferredUsername,
    tokenExpireTime: userDTO.data.accessTokenExpiration
  });
  LocalUUtils.setPropertyToSessionStorage({ accessToken, refreshToken });
  const userInfoDTO = await userRepository.userInfo(userDTO.data.userId);
  user.email = userInfoDTO.data.email;
  user.firstName = userInfoDTO.data.firstName;
  user.lastName = userInfoDTO.data.lastName;
  //calculate difference between now and expiration
  const remain = userDTO.data.accessTokenExpiration - dayjs().unix();
  timeOut((remain - 10) * 1000);
  return user;
};

const refreshToken = async () => {
  try {
    const currentTokens = LocalUUtils.getTokens();
    const userDTO = await userRepository.refreshToken(currentTokens.refreshToken);
    const { accessToken, refreshToken } = userDTO.data;
    const user = new User({
      accessRole: userDTO.data.roles,
      contextRoles: userDTO.data.groups,
      id: userDTO.data.userId,
      name: userDTO.data.preferredUsername,
      preferredUsername: userDTO.data.preferredUsername,
      tokenExpireTime: userDTO.data.accessTokenExpiration
    });
    LocalUUtils.setPropertyToSessionStorage({ accessToken, refreshToken });
    const userInfoDTO = await userRepository.userInfo(userDTO.data.userId);
    user.email = userInfoDTO.data.email;
    user.firstName = userInfoDTO.data.firstName;
    user.lastName = userInfoDTO.data.lastName;
    //calculate difference between now and expiration
    const remain = userDTO.data.accessTokenExpiration - dayjs().unix();
    timeOut((remain - 10) * 1000);
    return user;
  } catch (error) {
    LocalUUtils.remove();
  }
};

const userRole = (user, entity) => {
  const roleDTO = user.contextRoles.filter(role => role.includes(entity));
  if (roleDTO.length) {
    const [roleName] = roleDTO[0].split('-').reverse();
    return config.permissions.roles[roleName];
  }
  return;
};

const getToken = () => {
  return LocalUserStorageUtils?.getTokens()?.accessToken;
};

export const ApiUserRepository = {
  getConfiguration,
  getToken,
  login,
  logout,
  oldLogin,
  refreshToken,
  updateAttributes,
  userInfo,
  userRole
};
