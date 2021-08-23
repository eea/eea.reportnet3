import capitalize from 'lodash/capitalize';
import pick from 'lodash/pick';

const editLargeStringWithDots = (string, length) => {
  if (string.length > length) {
    return string.substring(0, length).concat('...');
  } else {
    return string;
  }
};

const getColumnByHeader = (columns, header) => columns.filter(e => e.header === header)[0];

const getFieldValues = (columns, header, filterColumns) => {
  const filteredColumn = columns.filter(e => {
    return e.header === header;
  })[0];

  const filteredValues = pick(filteredColumn, ...filterColumns);
  return Object.keys(filteredValues).map(key => {
    return {
      field:
        key === 'codelistItems'
          ? filteredValues.type === 'CODELIST'
            ? 'Single select items'
            : 'Multiple select items'
          : key === 'validExtensions'
          ? 'Valid extensions'
          : key === 'maxSize'
          ? 'Maximum file size'
          : key === 'readOnly'
          ? 'Read only'
          : capitalize(key),
      value:
        filteredValues[key] === 'CODELIST'
          ? 'SINGLE SELECT'
          : filteredValues[key] === 'MULTISELECT_CODELIST'
          ? 'MULTIPLE SELECT'
          : Array.isArray(filteredValues[key])
          ? filteredValues[key].join(key === 'codelistItems' ? '; ' : ', ')
          : filteredValues[key]
    };
  });
};

const getLevelError = validations => {
  let levelError = '';
  let lvlFlag = 0;
  const errors = [];
  validations.forEach(validation => {
    errors.push(validation.levelError);
  });

  validations.forEach(validation => {
    if (validation.levelError === 'INFO') {
      const iNum = 1;
      if (iNum > lvlFlag) {
        lvlFlag = iNum;
        levelError = 'INFO';
      }
    } else if (validation.levelError === 'WARNING') {
      const wNum = 2;
      if (wNum > lvlFlag) {
        lvlFlag = wNum;
        levelError = 'WARNING';
      }
    } else if (validation.levelError === 'ERROR') {
      const eNum = 3;
      if (eNum > lvlFlag) {
        lvlFlag = eNum;
        levelError = 'ERROR';
      }
    } else if (validation.levelError === 'BLOCKER') {
      const bNum = 4;
      if (bNum > lvlFlag) {
        lvlFlag = bNum;
        levelError = 'BLOCKER';
      }
    }
  });
  return levelError;
};

const formatValidations = validations => {
  let message = '';
  const errorValidations = [...new Set(validations.map(validation => validation.levelError))];
  validations.forEach(validation => {
    let error = '';
    if (errorValidations.length > 1) {
      error = `${capitalize(validation.levelError)}: `;
    }
    message += '- ' + error + validation.message + '\n';
  });
  return message;
};

const orderValidationsByLevelError = validations => {
  return validations
    .sort((a, b) => {
      const levelErrorsWithPriority = [
        { id: 'INFO', index: 1 },
        { id: 'WARNING', index: 2 },
        { id: 'ERROR', index: 3 },
        { id: 'BLOCKER', index: 4 }
      ];
      let levelError = levelErrorsWithPriority.filter(priority => a.levelError === priority.id)[0].index;
      let levelError2 = levelErrorsWithPriority.filter(priority => b.levelError === priority.id)[0].index;
      return levelError < levelError2 ? -1 : levelError > levelError2 ? 1 : 0;
    })
    .reverse();
};

const parseData = data =>
  data.records.map(record => {
    const datasetPartitionId = record.datasetPartitionId;
    const providerCode = record.providerCode;
    const recordValidations = record.validations;
    const recordId = record.recordId;
    const recordSchemaId = record.recordSchemaId;
    const arrayDataFields = record.fields.map(field => {
      return {
        fieldData: {
          [field.fieldSchemaId]: field.value,
          type: field.type,
          id: field.fieldId,
          fieldSchemaId: field.fieldSchemaId
        },
        fieldValidations: field.validations
      };
    });
    arrayDataFields.push({ fieldData: { id: record.recordId }, fieldValidations: null });
    arrayDataFields.push({ fieldData: { datasetPartitionId: record.datasetPartitionId }, fieldValidations: null });
    const arrayDataAndValidations = {
      dataRow: arrayDataFields,
      recordValidations,
      recordId,
      datasetPartitionId,
      providerCode,
      recordSchemaId
    };
    return arrayDataAndValidations;
  });

export const DataViewerUtils = {
  editLargeStringWithDots,
  formatValidations,
  getColumnByHeader,
  getFieldValues,
  getLevelError,
  orderValidationsByLevelError,
  parseData
};
