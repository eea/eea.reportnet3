import { config } from 'conf/index';

import { UserRepository } from 'repositories/UserRepository';

import { User } from 'entities/User';

import { LocalUserStorageUtils } from 'services/_utils/LocalUserStorageUtils';
import { UserUtils } from 'services/_utils/UserUtils';

const refreshToken = async () => {
  try {
    const currentTokens = LocalUserStorageUtils.getTokens();
    const userDTO = await UserRepository.refreshToken(currentTokens.refreshToken);
    return calculateUser(userDTO);
  } catch (error) {
    LocalUserStorageUtils.remove();
  }
};

const calculateUser = async userDTO => {
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

  setTimeout(() => {
    refreshToken();
  }, userDTO.data.accessTokenExpiration);

  return user;
};

export const UserService = {
  login: async code => {
    const userDTO = await UserRepository.login(code);
    return calculateUser(userDTO);
  },

  logout: async () => {
    const currentTokens = LocalUserStorageUtils.getTokens();
    LocalUserStorageUtils.remove();

    if (!currentTokens) {
      return;
    }

    return await UserRepository.logout(currentTokens.refreshToken);
  },

  getUserInfo: async userId => {
    const userDTO = await UserRepository.getUserInfo(userId);
    return new User({
      email: userDTO.email,
      firstName: userDTO.firstName,
      lastName: userDTO.lastName
    });
  },

  getConfiguration: async () => {
    const userConfigurationDTO = await UserRepository.getConfiguration();
    return UserUtils.parseConfigurationDTO(userConfigurationDTO);
  },

  updateConfiguration: async attributes => await UserRepository.updateConfiguration(attributes),

  oldLogin: async (userName, password) => {
    const userDTO = await UserRepository.oldLogin(userName, password);
    return calculateUser(userDTO);
  },

  refreshToken,

  getUserRole: (user, entity) => {
    const roleDTO = user.contextRoles.filter(role => role.includes(entity));
    if (roleDTO.length === 0) {
      return;
    }
    const [roleName] = roleDTO[0].split('-').reverse();
    return config.permissions.roles[roleName];
  }
};
