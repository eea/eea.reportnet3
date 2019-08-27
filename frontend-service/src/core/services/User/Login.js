export const Login = ({ userRepository }) => async (userName, password) => userRepository.login(userName, password);
