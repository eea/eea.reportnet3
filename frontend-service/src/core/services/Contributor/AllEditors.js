export const AllEditors = ({ contributorRepository }) => async dataflowId =>
  contributorRepository.allEditors(dataflowId);
