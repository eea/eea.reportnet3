export const HasPermission = ({ userRepository }) => (user, permissions, entity) =>
  userRepository.hasPermission(user, permissions, entity);
