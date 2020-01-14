export const Update = ({ codelistRepository }) => async (id, description, items, name, status, version) =>
  codelistRepository.updateById(id, description, items, name, status, version);
