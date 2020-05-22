export const Update = ({ uniqueConstraintsRepository }) => async (description, fieldId, fieldSchemaId, name) =>
  uniqueConstraintsRepository.update(description, fieldId, fieldSchemaId, name);
