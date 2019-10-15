import { ApiUserRepository } from 'core/infrastructure/domain/model/User/ApiUserRepository';

export const UserRepository = {
  login: () => Promise.reject('[UserRepository#login] must be implemented'),
  logout: () => Promise.reject('[UserRepository#logout] must be implemented'),
  oldLogin: () => Promise.reject('[UserRepository#logout] must be implemented'),
  refreshToken: () => Promise.reject('[UserRepository#refreshToken] must be implemented'),
  hasPermission: () => '[UserRepository#hasPermission] must be implemented',
  userRole: () => '[UserRepository#userRole] must be implemented'
};

export const userRepository = Object.assign({}, UserRepository, ApiUserRepository);
