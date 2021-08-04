export const Download = ({ representativeRepository }) => async dataflowId =>
  representativeRepository.downloadById(dataflowId);
