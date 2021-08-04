import { GetConfiguration } from './GetConfiguration';
import { GetToken } from './GetToken';
import { Login } from './Login';
import { Logout } from './Logout';
import { OldLogin } from './OldLogin';
import { RefreshToken } from './RefreshToken';
import { updateAttributes } from './updateAttributes';
import { userRepository } from 'entities/User/UserRepository';
import { UserRole } from './UserRole';

export const UserService = {
  getConfiguration: GetConfiguration({ userRepository }),
  getToken: GetToken({ userRepository }),
  login: Login({ userRepository }),
  logout: Logout({ userRepository }),
  oldLogin: OldLogin({ userRepository }),
  refreshToken: RefreshToken({ userRepository }),
  updateAttributes: updateAttributes({ userRepository }),
  userRole: UserRole({ userRepository })
};
