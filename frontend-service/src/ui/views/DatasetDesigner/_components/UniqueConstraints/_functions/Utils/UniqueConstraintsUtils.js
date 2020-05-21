const parseConstraintsList = (constraintsData, tableData) => {
  const allData = constraintsData.map((constraint, index) => Object.assign({}, constraint, tableData[index]));
  const constraints = [];
  allData.forEach(constraintDTO => constraints.push(parseConstraint(constraintDTO)));

  return constraints;
};

const parseConstraint = data => ({
  tableSchemaId: data.tableSchemaId,
  tableSchemaName: data.tableSchemaName,
  fieldData: parseFieldsData(data.fieldSchemaIds, data.records[0]),
  uniqueId: data.uniqueId,
  filterFieldsNames: parseFieldsData(data.fieldSchemaIds, data.records[0])
    .map(field => field.name)
    .join(', ')
});

const parseFieldsData = (fields, record) => record.fields.filter(field => !fields.includes(field));

export const UniqueConstraintsUtils = { parseConstraintsList };
