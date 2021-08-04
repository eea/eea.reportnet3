export const UserConfig = {
  configuration: '/user/getAttributes',
  login: '/user/generateTokenByCode?code={:code}',
  logout: '/user/logout?refreshToken={:refreshToken}',
  oldLogin: '/user/generateToken?password={:password}&username={:userName}',
  refreshToken: '/user/refreshToken?refreshToken={:refreshToken}',
  updateConfiguration: '/user/updateAttributes',
  userInfo: '/user/getUserByUserId?userId={:userId}'
};
