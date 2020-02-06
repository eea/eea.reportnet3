export const AllRepresentatives = ({ representativeRepository }) => async dataflowId =>
  representativeRepository.allRepresentatives(dataflowId);
