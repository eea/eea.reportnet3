import dayjs from 'dayjs';

import { config } from 'conf/index';

import { UserRepository } from 'repositories/UserRepository';

import { User } from 'entities/User';

import { LocalUserStorageUtils } from 'services/_utils/LocalUserStorageUtils';
import { UserUtils } from 'services/_utils/UserUtils';

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
    return UserUtils.parseConfigurationDTO(userConfigurationDTO);
  },

  updateConfiguration: async attributes => await UserRepository.updateConfiguration(attributes),

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
  }
};
