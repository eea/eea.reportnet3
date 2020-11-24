export const OldLogin = ({ userRepository }) => async (userName, password) =>
  userRepository.oldLogin(userName, password);
