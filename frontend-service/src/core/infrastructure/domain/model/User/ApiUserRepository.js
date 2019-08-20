import { Api, api } from 'core/infrastructure/api';
import { User } from 'core/domain/model/User/User';
import jwt_decode from 'jwt-decode';

const login = async (userName, password) => {
  try {
    // call to login endpoint
    const userTokensTDO = await api.login(userName, password);
    // decode response first element jwt token
    const userTDO = jwt_decode(userTokensTDO.accessToken);
    // new user object
    // save to webStorage
    // return user
  } catch (error) {
    console.error(error);
  }
};
const logout = async userId => {};
const refreshToken = async refreshToken => {};

export const ApiUserRepository = {
  login,
  logout,
  refreshToken
};
