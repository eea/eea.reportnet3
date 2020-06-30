export const All = ({ contributorRepository }) => async dataflowId => {
  contributorRepository.all(dataflowId);
};
