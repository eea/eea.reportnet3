export const UpdateEditor = ({ contributorRepository }) => async (editor, dataflowId) =>
  contributorRepository.updateEditor(editor, dataflowId);
