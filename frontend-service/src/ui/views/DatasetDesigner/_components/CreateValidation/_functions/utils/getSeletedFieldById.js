import isNil from 'lodash/isNil';

export const getSelectedFieldById = (fieldId, tables) => {
  let selectedField = {};
  tables.forEach(table => {
    if (!isNil(table.records)) {
      const [candidateField] = table.records[0].fields.filter(field => field.fieldId == fieldId);
      if (candidateField) {
        console.log('candidateField', candidateField);
        selectedField = {
          label: candidateField.name,
          code: candidateField.fieldId
        };
      }
    }
  });
  return selectedField;
};
