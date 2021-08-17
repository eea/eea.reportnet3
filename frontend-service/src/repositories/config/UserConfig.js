export const UserConfig = {
  getConfiguration: '/user/getAttributes',
  getUserInfo: '/user/getUserByUserId?userId={:userId}',
  login: '/user/generateTokenByCode?code={:code}',
  logout: '/user/logout?refreshToken={:refreshToken}',
  oldLogin: '/user/generateToken?password={:password}&username={:userName}',
  refreshToken: '/user/refreshToken?refreshToken={:refreshToken}',
  updateConfiguration: '/user/updateAttributes'
};
