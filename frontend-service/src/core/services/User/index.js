import { Login } from './Login';
import { Logout } from './Logout';
import { RefreshToken } from './RefreshToken';
import { userRepository } from 'core/domain/model/User/UserRepository';

export const UserService = {
  login: Login({ userRepository }),
  logout: Logout({ userRepository }),
  refreshToken: RefreshToken({ userRepository })
};
