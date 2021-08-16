export const GetRepresentativesUsersList = ({ dataflowRepository }) => async dataflowId =>
  dataflowRepository.getRepresentativesUsersList(dataflowId);
