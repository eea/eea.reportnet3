export const Edit = ({ businessDataflowRepository }) => async (
  dataflowId,
  description,
  name,
  type,
  groupCompaniesId,
  fmeUserId
) => businessDataflowRepository.edit(dataflowId, description, name, type, groupCompaniesId, fmeUserId);
