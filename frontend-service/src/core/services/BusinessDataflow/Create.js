export const Create = ({ businessDataflowRepository }) => async (
  name,
  description,
  obligationId,
  type,
  groupCompaniesId,
  fmeUserId
) => businessDataflowRepository.create(name, description, obligationId, type, groupCompaniesId, fmeUserId);
