export const RefreshToken = ({ userRepository }) => async refreshToken => userRepository.refreshToken(refreshToken);
