const parseConstraintsList = (constraintsData, tableData) => {
  const constraints = [];
  for (let i = 0; i < constraintsData.length; i++) {
    constraints.push(
      parseConstraint({
        ...constraintsData[i],
        ...tableData.find(table => table.tableSchemaId === constraintsData[i].tableSchemaId)
      })
    );
  }
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

const parseFieldsData = (fields, record) => record.fields.filter(field => fields.includes(field.fieldId));

export const UniqueConstraintsUtils = { parseConstraintsList };
