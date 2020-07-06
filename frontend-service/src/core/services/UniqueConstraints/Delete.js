export const Delete = ({ uniqueConstraintsRepository }) => async (uniqueConstraintId, dataflowId) =>
  uniqueConstraintsRepository.deleteById(uniqueConstraintId, dataflowId);
