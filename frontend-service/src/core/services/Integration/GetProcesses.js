export const GetProcesses = ({ integrationRepository }) => async repository =>
  integrationRepository.getProcesses(repository);
