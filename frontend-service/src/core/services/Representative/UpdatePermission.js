export const UpdatePermission = ({ representativeRepository }) => async (representativeId, permission) =>
  representativeRepository.UpdatePermission(representativeId, permission);
