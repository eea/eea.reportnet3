import dayjs from 'dayjs';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf/index';

import { UserRepository } from 'repositories/UserRepository';
import { User } from 'entities/User';
import { LocalUserStorageUtils } from './_utils/LocalUserStorageUtils';

const refreshToken = async () => {
  try {
    const currentTokens = LocalUserStorageUtils.getTokens();
    const userDTO = await UserRepository.refreshToken(currentTokens.refreshToken);
    const { accessToken, refreshToken } = userDTO.data;
    const user = new User({
      accessRole: userDTO.data.roles,
      contextRoles: userDTO.data.groups,
      id: userDTO.data.userId,
      name: userDTO.data.preferredUsername,
      preferredUsername: userDTO.data.preferredUsername,
      tokenExpireTime: userDTO.data.accessTokenExpiration
    });
    LocalUserStorageUtils.setPropertyToSessionStorage({ accessToken, refreshToken });
    const userInfoDTO = await UserRepository.getUserInfo(userDTO.data.userId);
    user.email = userInfoDTO.data.email;
    user.firstName = userInfoDTO.data.firstName;
    user.lastName = userInfoDTO.data.lastName;
    //calculate difference between now and expiration
    const remain = userDTO.data.accessTokenExpiration - dayjs().unix();
    timeOut((remain - 10) * 1000);
    return user;
  } catch (error) {
    LocalUserStorageUtils.remove();
  }
};

const timeOut = time => {
  setTimeout(() => {
    refreshToken();
  }, time);
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

export const UserService = {
  login: async code => {
    const userDTO = await UserRepository.login(code);
    const { accessToken, refreshToken } = userDTO.data;
    const user = new User({
      accessRole: userDTO.data.roles,
      contextRoles: userDTO.data.groups,
      id: userDTO.data.userId,
      name: userDTO.data.preferredUsername,
      preferredUsername: userDTO.data.preferredUsername,
      tokenExpireTime: userDTO.data.accessTokenExpiration
    });
    LocalUserStorageUtils.setPropertyToSessionStorage({ accessToken, refreshToken });
    const userInfoDTO = await UserRepository.getUserInfo(userDTO.data.userId);
    user.email = userInfoDTO.data.email;
    user.firstName = userInfoDTO.data.firstName;
    user.lastName = userInfoDTO.data.lastName;
    //calculate difference between now and expiration
    const remain = userDTO.data.accessTokenExpiration - dayjs().unix();
    timeOut((remain - 10) * 1000);
    return user;
  },

  logout: async () => {
    const currentTokens = LocalUserStorageUtils.getTokens();
    LocalUserStorageUtils.remove();
    if (currentTokens) {
      const response = await UserRepository.logout(currentTokens.refreshToken);
      return response;
    }
    return;
  },

  getUserInfo: async userId => {
    const userDTO = await UserRepository.getUserInfo(userId);
    const user = new User({
      email: userDTO.email,
      firstName: userDTO.firstName,
      lastName: userDTO.lastName
    });

    return user;
  },

  getConfiguration: async () => {
    const userConfigurationDTO = await UserRepository.getConfiguration();
    return parseConfigurationDTO(userConfigurationDTO);
  },

  updateAttributes: async attributes => await UserRepository.updateAttributes(attributes),

  oldLogin: async (userName, password) => {
    const userDTO = await UserRepository.oldLogin(userName, password);

    const { accessToken, refreshToken } = userDTO.data;
    const user = new User({
      accessRole: userDTO.data.roles,
      contextRoles: userDTO.data.groups,
      id: userDTO.data.userId,
      name: userDTO.data.preferredUsername,
      preferredUsername: userDTO.data.preferredUsername,
      tokenExpireTime: userDTO.data.accessTokenExpiration
    });
    LocalUserStorageUtils.setPropertyToSessionStorage({ accessToken, refreshToken });
    const userInfoDTO = await UserRepository.getUserInfo(userDTO.data.userId);
    user.email = userInfoDTO.data.email;
    user.firstName = userInfoDTO.data.firstName;
    user.lastName = userInfoDTO.data.lastName;
    //calculate difference between now and expiration
    const remain = userDTO.data.accessTokenExpiration - dayjs().unix();
    timeOut((remain - 10) * 1000);
    return user;
  },

  refreshToken,

  getUserRole: (user, entity) => {
    const roleDTO = user.contextRoles.filter(role => role.includes(entity));
    if (roleDTO.length) {
      const [roleName] = roleDTO[0].split('-').reverse();
      return config.permissions.roles[roleName];
    }
    return;
  },

  getToken: () => {
    return LocalUserStorageUtils?.getTokens()?.accessToken;
  }
};
