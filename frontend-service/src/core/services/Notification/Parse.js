export const Parse = ({ notificationRepository }) => (notification, config, routes) =>
  notificationRepository.parse(notification, config, routes);
