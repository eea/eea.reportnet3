export const Delete = ({ codelistRepository }) => async codelistId => codelistRepository.deleteById(codelistId);
