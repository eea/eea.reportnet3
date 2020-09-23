export const Delete = ({ uniqueConstraintsRepository }) => async (dataflowId, uniqueConstraintId) =>
  uniqueConstraintsRepository.deleteById(dataflowId, uniqueConstraintId);
