import lowerCase from 'lodash/lowerCase';

export const parseFields = rawFields => {
  const fields = [];
  const fieldsOptions = [];

  for (const field of rawFields) {
    const { name, fieldId, type } = field;
    fieldsOptions.push({ label: `${name} (${lowerCase(type)})`, value: fieldId });
    fields.push({ name, fieldId });
  }
  return { fields, fieldsOptions };
};
