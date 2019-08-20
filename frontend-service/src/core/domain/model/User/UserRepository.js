import { ApiUserRepository } from 'core/infrastructure/domain/model/User/ApiUserRepository';

export const UserRepository = {
  login: () => Promise.reject('[UserRepository#login] must be implemented'),
  logout: () => Promise.reject('[UserRepository#logout] must be implemented'),
  refreshToken: () => '[UserRepository#refreshToken] must be implemented'
};
