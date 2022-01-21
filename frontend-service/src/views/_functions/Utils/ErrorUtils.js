import { IconTooltip } from 'views/_components/IconTooltip';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import snakeCase from 'lodash/snakeCase';

import { ValidationUtils } from 'services/_utils/ValidationUtils';

const getGroupValidations = (recordData, blockerMessage, errorMessage, warningMessage, infoMessage) => {
  let validations = [];
  if (recordData?.recordValidations) {
    validations = [...recordData.recordValidations];
  }

  const recordsWithFieldValidations = recordData?.dataRow.filter(row => !isNil(row.fieldValidations)) || [];

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

const addIconLevelError = (validation, levelError, message) => {
  if (isEmpty(validation)) {
    return [];
  }

  return [].concat(<IconTooltip key={levelError} levelError={levelError} message={message} />);
};

const getIconsValidationsErrors = validations => {
  if (isNil(validations)) {
    return [];
  }

  const blockerIcon = addIconLevelError(validations.blockers, 'BLOCKER', validations.messageBlockers);
  const errorIcon = addIconLevelError(validations.errors, 'ERROR', validations.messageErrors);
  const warningIcon = addIconLevelError(validations.warnings, 'WARNING', validations.messageWarnings);
  const infoIcon = addIconLevelError(validations.infos, 'INFO', validations.messageInfos);

  return blockerIcon.concat(errorIcon, warningIcon, infoIcon);
};

const getValidationsTemplate = (data, messages) => {
  const validationsGroup = getGroupValidations(
    data,
    messages.blockers,
    messages.errors,
    messages.warnings,
    messages.infos
  );

  return getIconsValidationsErrors(validationsGroup);
};

const getLevelErrorPriorityByLevelError = levelError => {
  switch (levelError) {
    case 'CORRECT':
      return 0;
    case 'INFO':
      return 1;
    case 'WARNING':
      return 2;
    case 'ERROR':
      return 3;
    case 'BLOCKER':
      return 4;
    case '':
      return 99;
    default:
      return null;
  }
};

const orderLevelErrors = levelErrors => {
  const levelErrorsWithPriority = [
    { id: 'CORRECT', index: 0 },
    { id: 'INFO', index: 1 },
    { id: 'WARNING', index: 2 },
    { id: 'ERROR', index: 3 },
    { id: 'BLOCKER', index: 4 }
  ];

  return levelErrors
    .map(error => levelErrorsWithPriority.filter(e => error === e.id))
    .flat()
    .sort((a, b) => a.index - b.index)
    .map(orderedError => orderedError.id);
};

const parseErrorType = errorType => `${snakeCase(errorType).toUpperCase()}_ERROR`;

export const ErrorUtils = {
  getGroupValidations,
  getLevelErrorPriorityByLevelError,
  getValidationsTemplate,
  orderLevelErrors,
  parseErrorType
};
