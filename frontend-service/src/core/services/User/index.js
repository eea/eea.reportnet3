import { Login } from './Login';
import { Logout } from './Logout';
import { RefreshToken } from './RefreshToken';
import { UserRepository } from 'core/domain/model/User/UserRepository';

export const UserService = {
  login: Login({ UserRepository }),
  logout: Logout({ UserRepository }),
  refreshToken: RefreshToken({ UserRepository })
};
