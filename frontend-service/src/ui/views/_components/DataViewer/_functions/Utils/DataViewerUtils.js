import { capitalize } from 'lodash';

export const DataViewerUtils = {
  parseData: data =>
    data.records.map(record => {
      const datasetPartitionId = record.datasetPartitionId;
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
        recordSchemaId
      };
      return arrayDataAndValidations;
    }),
  getLevelError: validations => {
    let levelError = '';
    let lvlFlag = 0;
    const errors = [];
    validations.forEach(validation => {
      errors.push(validation.levelError);
    });
    let differentErrors = [...new Set(errors)];

    if (differentErrors.length > 1) {
      return 'MULTI';
    } else {
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
    }
    return levelError;
  },
  orderValidationsByLevelError: validations => {
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
  },
  formatValidations: validations => {
    let message = '';
    const errorValidations = [...new Set(validations.map(validation => validation.levelError))];
    validations.forEach(validation => {
      let error = '';
      if (errorValidations.length > 1) {
        error = `${capitalize(validation.levelError)}: `;
      }
      message += '- ' + error + capitalize(validation.message) + '\n';
    });
    return message;
  }
};
