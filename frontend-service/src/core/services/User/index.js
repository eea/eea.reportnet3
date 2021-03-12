import { GetToken } from './GetToken';
import { GetConfiguration } from './GetConfiguration';
import { Login } from './Login';
import { Logout } from './Logout';
import { OldLogin } from './OldLogin';
import { RefreshToken } from './RefreshToken';
import { userRepository } from 'core/domain/model/User/UserRepository';
import { UserRole } from './UserRole';
import { updateAttributes } from './updateAttributes';

export const UserService = {
  login: Login({ userRepository }),
  logout: Logout({ userRepository }),
  oldLogin: OldLogin({ userRepository }),
  refreshToken: RefreshToken({ userRepository }),
  getConfiguration: GetConfiguration({ userRepository }),
  getToken: GetToken({ userRepository }),
  userRole: UserRole({ userRepository }),
  updateAttributes: updateAttributes({ userRepository })
};
