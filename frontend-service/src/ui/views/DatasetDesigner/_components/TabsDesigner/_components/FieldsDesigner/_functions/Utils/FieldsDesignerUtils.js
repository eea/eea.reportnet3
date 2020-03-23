import isNil from 'lodash/isNil';

const arrayShift = (arr, initialIdx, endIdx) => {
  const element = arr[initialIdx];
  if (endIdx === -1) {
    arr.splice(initialIdx, 1);
    arr.splice(arr.length, 0, element);
  } else {
    if (Math.abs(endIdx - initialIdx) > 1) {
      arr.splice(initialIdx, 1);
      if (initialIdx < endIdx) {
        arr.splice(endIdx - 1, 0, element);
      } else {
        arr.splice(endIdx, 0, element);
      }
    } else {
      if (endIdx === 0) {
        arr.splice(initialIdx, 1);
        arr.splice(0, 0, element);
      } else {
        arr.splice(initialIdx, 1);
        if (initialIdx < endIdx) {
          arr.splice(endIdx - 1, 0, element);
        } else {
          arr.splice(endIdx, 0, element);
        }
      }
    }
  }
  return arr;
};

const checkDuplicates = (fields, name, fieldId) => {
  if (!isNil(fields)) {
    const inmFields = [...fields];
    const repeteadElements = inmFields.filter(field => name.toLowerCase() === field.name.toLowerCase());
    return repeteadElements.length > 0 && fieldId !== repeteadElements[0].fieldId;
  } else {
    return false;
  }
};

const getIndexByFieldName = (fieldName, fieldsArray) => {
  return fieldsArray
    .map(field => {
      return field.name;
    })
    .indexOf(fieldName);
};

const getIndexByFieldId = (fieldId, fieldsArray) => {
  return fieldsArray
    .map(field => {
      return field.fieldId;
    })
    .indexOf(fieldId);
};

export const FieldsDesignerUtils = {
  arrayShift,
  checkDuplicates,
  getIndexByFieldName,
  getIndexByFieldId
};
