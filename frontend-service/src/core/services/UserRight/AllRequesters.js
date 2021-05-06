export const AllRequesters = ({ userRightRepository }) => async dataflowId =>
  userRightRepository.allRequesters(dataflowId);
