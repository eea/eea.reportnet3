export const UniqueConstraintConfig = {
  getAll: '/dataschema/{:datasetSchemaId}/getUniqueConstraints/dataflow/{:dataflowId}',
  create: '/dataschema/createUniqueConstraint',
  delete: '/dataschema/deleteUniqueConstraint/{:uniqueConstraintId}/dataflow/{:dataflowId}',
  update: '/dataschema/updateUniqueConstraint'
};
