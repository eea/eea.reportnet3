export const All = ({ contributorRepository }) => async dataflowId => {
  console.log('dataflowId', dataflowId);
  contributorRepository.all(dataflowId);
};
