import { GetToken } from './GetToken';
import { HasPermission } from './HasPermission';
import { Login } from './Login';
import { Logout } from './Logout';
import { OldLogin } from './OldLogin';
import { RefreshToken } from './RefreshToken';
import { userRepository } from 'core/domain/model/User/UserRepository';
import { UserRole } from './UserRole';

export const UserService = {
  login: Login({ userRepository }),
  logout: Logout({ userRepository }),
  oldLogin: OldLogin({ userRepository }),
  refreshToken: RefreshToken({ userRepository }),
  hasPermission: HasPermission({ userRepository }),
  getToken: GetToken({ userRepository }),
  userRole: UserRole({ userRepository })
};
