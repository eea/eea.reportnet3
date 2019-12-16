import { capitalize, isUndefined, isNull, isEmpty } from 'lodash';

import { DatasetService } from 'core/services/Dataset';

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
  },
  groupValidations: (recordData, blockerMessage, errorMessage, warningMessage, infoMessage) => {
    let validations = [];
    if (recordData.recordValidations && !isUndefined(recordData.recordValidations)) {
      validations = [...recordData.recordValidations];
    }

    const recordsWithFieldValidations = recordData.dataRow.filter(
      row => !isUndefined(row.fieldValidations) && !isNull(row.fieldValidations)
    );

    const getRecordValidationByErrorAndMessage = (levelError, message) => {
      return DatasetService.createValidation('RECORD', 0, levelError, message);
    };

    const filteredFieldValidations = recordsWithFieldValidations.map(record => record.fieldValidations).flat();
    if (!isEmpty(recordsWithFieldValidations)) {
      const filterFieldValidation = (errorType, errorMessage) => {
        const filteredFieldValidationsWithErrorType = filteredFieldValidations.filter(
          filteredFieldValidation => filteredFieldValidation.levelError === errorType
        );
        if (!isEmpty(filteredFieldValidationsWithErrorType)) {
          validations.push(getRecordValidationByErrorAndMessage(errorType, errorMessage));
        }
      };
      filterFieldValidation('BLOCKER', blockerMessage);
      filterFieldValidation('ERROR', errorMessage);
      filterFieldValidation('WARNING', warningMessage);
      filterFieldValidation('INFO', infoMessage);
    }

    const blockerValidations = validations.filter(validation => validation.levelError === 'BLOCKER');
    const errorValidations = validations.filter(validation => validation.levelError === 'ERROR');
    const warningValidations = validations.filter(validation => validation.levelError === 'WARNING');
    const infoValidations = validations.filter(validation => validation.levelError === 'INFO');

    const getMessages = validationsType => {
      let messageType = '';
      validationsType.forEach(validation =>
        validation.message ? (messageType += '- ' + capitalize(validation.message) + '\n') : ''
      );
      return messageType;
    };

    const validationsGroup = {
      blockers: blockerValidations,
      errors: errorValidations,
      warnings: warningValidations,
      infos: infoValidations,
      messageBlockers: getMessages(blockerValidations),
      messageErrors: getMessages(errorValidations),
      messageWarnings: getMessages(warningValidations),
      messageInfos: getMessages(infoValidations)
    };
    return validationsGroup;
  }
};
