export const Delete = ({ representativeRepository }) => async representativeId =>
  representativeRepository.deleteById(representativeId);
