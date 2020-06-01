export const Delete = ({ uniqueConstraintsRepository }) => async uniqueConstraintId =>
  uniqueConstraintsRepository.deleteById(uniqueConstraintId);
