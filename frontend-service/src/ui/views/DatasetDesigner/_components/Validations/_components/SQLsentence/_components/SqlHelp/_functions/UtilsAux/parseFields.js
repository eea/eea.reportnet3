import lowerCase from 'lodash/lowerCase';

import { config } from 'conf';

export const parseFields = rawFields => {
  const fields = [];
  const fieldsOptions = [];
  const {
    validations: {
      bannedTypes: { sqlFields }
    }
  } = config;

  for (const field of rawFields) {
    const { name, fieldId, type } = field;
    if (!sqlFields.includes(lowerCase(type))) {
      fieldsOptions.push({ label: `${name} (${lowerCase(type)})`, value: fieldId });
      fields.push({ name, fieldId });
    }
  }
  return { fields, fieldsOptions };
};
