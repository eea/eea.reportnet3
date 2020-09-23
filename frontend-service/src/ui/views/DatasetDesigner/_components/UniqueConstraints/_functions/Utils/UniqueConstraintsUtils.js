import uniqBy from 'lodash/uniqBy';

const getFieldsOptions = data => {
  const parsedFields = data.map(unique => unique.fieldData.map(item => ({ id: item.fieldId, name: item.name })));
  const allFields = [];
  for (let index = 0; index < parsedFields.length; index++) {
    allFields.push(...parsedFields[index]);
  }
  return uniqBy(allFields, 'id');
};

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
  uniqueId: data.uniqueId
});

const parseFieldsData = (fields, record) => record.fields.filter(field => fields.includes(field.fieldId));

export const UniqueConstraintsUtils = { getFieldsOptions, parseConstraintsList };
