export const Delete = ({ integrationRepository }) => async integrationId =>
  integrationRepository.deleteById(integrationId);
