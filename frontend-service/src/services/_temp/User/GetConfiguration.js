export const GetConfiguration = ({ userRepository }) => async () =>
userRepository.getConfiguration();