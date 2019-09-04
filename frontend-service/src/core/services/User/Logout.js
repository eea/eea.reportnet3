export const Logout = ({ userRepository }) => async userId => userRepository.logout(userId);
