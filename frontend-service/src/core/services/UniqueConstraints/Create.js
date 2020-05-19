export const Create = ({ uniqueConstraintsRepository }) => async (description, fieldSchemaId, name) =>
  uniqueConstraintsRepository.create(description, fieldSchemaId, name);
