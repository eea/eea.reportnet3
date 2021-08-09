import isEmpty from 'lodash/isEmpty';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import { ValidationUtils } from 'services/_utils/ValidationUtils';

const getGroupValidations = (recordData, blockerMessage, errorMessage, warningMessage, infoMessage) => {
  let validations = [];
  if (recordData?.recordValidations) {
    validations = [...recordData.recordValidations];
  }

  const recordsWithFieldValidations =
    recordData?.dataRow.filter(row => !isUndefined(row.fieldValidations) && !isNull(row.fieldValidations)) || [];

  const getRecordValidationByErrorAndMessage = (levelError, message) =>
    ValidationUtils.createValidation('RECORD', 0, levelError, message);

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
      validation.message ? (messageType += '- ' + validation.message + '\n') : ''
    );
    return messageType;
  };

  const validationsGroup = {
    blockers: blockerValidations,
    errors: errorValidations,
    infos: infoValidations,
    messageBlockers: getMessages(blockerValidations),
    messageErrors: getMessages(errorValidations),
    messageInfos: getMessages(infoValidations),
    messageWarnings: getMessages(warningValidations),
    warnings: warningValidations
  };
  return validationsGroup;
};

export const GroupedRecordValidationsUtils = { getGroupValidations };
