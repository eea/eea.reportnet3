// import { useContext } from 'react';
// import { isUndefined, isNull, isEmpty, capitalize } from 'lodash';

// import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

// import { DatasetService } from 'core/services/Dataset';

// const resources = useContext(ResourcesContext);

// export const DataViewerUtils = {
//   getRecordValidationByErrorAndMessage: (levelError, message) => {
//     return DatasetService.createValidation('RECORD', 0, levelError, message);
//   },
//   getIconsValidationsErrors: validations => {
//     let icons = [];
//     if (isNull(validations)) {
//       return icons;
//     }

//     let blockerIcon = DataViewerUtils.addIconLevelError(validations.blockers, 'BLOCKER', validations.messageBlockers);
//     let errorIcon = DataViewerUtils.addIconLevelError(validations.errors, 'ERROR', validations.messageErrors);
//     let warningIcon = DataViewerUtils.addIconLevelError(validations.warnings, 'WARNING', validations.messageWarnings);
//     let infoIcon = DataViewerUtils.addIconLevelError(validations.infos, 'INFO', validations.messageInfos);

//     icons = blockerIcon.concat(errorIcon, warningIcon, infoIcon);
//     return <div className={styles.iconTooltipWrapper}>{icons}</div>;
//   },
//   validationsTemplate: recordData => {
//     //Template for Record validation
//     let validations = [];
//     if (recordData.recordValidations && !isUndefined(recordData.recordValidations)) {
//       validations = [...recordData.recordValidations];
//     }

//     let hasFieldErrors = false;

//     const recordsWithFieldValidations = recordData.dataRow.filter(
//       row => !isUndefined(row.fieldValidations) && !isNull(row.fieldValidations)
//     );

//     hasFieldErrors = !isEmpty(recordsWithFieldValidations);

//     const filteredFieldValidations = recordsWithFieldValidations.map(record => record.fieldValidations).flat();

//     if (hasFieldErrors) {
//       const filteredFieldValidationsWithBlocker = filteredFieldValidations.filter(
//         filteredFieldValidation => filteredFieldValidation.levelError === 'BLOCKER'
//       );
//       if (!isEmpty(filteredFieldValidationsWithBlocker)) {
//         validations.push(
//           DataViewerUtils.getRecordValidationByErrorAndMessage('BLOCKER', resources.messages['recordBlockers'])
//         );
//       }

//       const filteredFieldValidationsWithError = filteredFieldValidations.filter(
//         filteredFieldValidation => filteredFieldValidation.levelError === 'ERROR'
//       );
//       if (!isEmpty(filteredFieldValidationsWithError)) {
//         validations.push(
//           DataViewerUtils.getRecordValidationByErrorAndMessage('ERROR', resources.messages['recordErrors'])
//         );
//       }

//       const filteredFieldValidationsWithWarning = filteredFieldValidations.filter(
//         filteredFieldValidation => filteredFieldValidation.levelError === 'WARNING'
//       );
//       if (!isEmpty(filteredFieldValidationsWithWarning)) {
//         validations.push(
//           DataViewerUtils.getRecordValidationByErrorAndMessage('WARNING', resources.messages['recordWarnings'])
//         );
//       }

//       const filteredFieldValidationsWithInfo = filteredFieldValidations.filter(
//         filteredFieldValidation => filteredFieldValidation.levelError === 'INFO'
//       );
//       if (!isEmpty(filteredFieldValidationsWithInfo)) {
//         validations.push(
//           DataViewerUtils.getRecordValidationByErrorAndMessage('INFO', resources.messages['recordInfos'])
//         );
//       }
//     }

//     const blockerValidations = validations.filter(validation => validation.levelError === 'BLOCKER');
//     const errorValidations = validations.filter(validation => validation.levelError === 'ERROR');
//     const warningValidations = validations.filter(validation => validation.levelError === 'WARNING');
//     const infoValidations = validations.filter(validation => validation.levelError === 'INFO');

//     let messageBlockers = '';
//     let messageErrors = '';
//     let messageWarnings = '';
//     let messageInfos = '';

//     blockerValidations.forEach(validation =>
//       validation.message ? (messageBlockers += '- ' + capitalize(validation.message) + '\n') : ''
//     );

//     errorValidations.forEach(validation =>
//       validation.message ? (messageErrors += '- ' + capitalize(validation.message) + '\n') : ''
//     );

//     warningValidations.forEach(validation =>
//       validation.message ? (messageWarnings += '- ' + capitalize(validation.message) + '\n') : ''
//     );

//     infoValidations.forEach(validation =>
//       validation.message ? (messageInfos += '- ' + capitalize(validation.message) + '\n') : ''
//     );

//     let validationsGroup = {};
//     validationsGroup.blockers = blockerValidations;
//     validationsGroup.errors = errorValidations;
//     validationsGroup.warnings = warningValidations;
//     validationsGroup.infos = infoValidations;

//     validationsGroup.messageBlockers = messageBlockers;
//     validationsGroup.messageErrors = messageErrors;
//     validationsGroup.messageWarnings = messageWarnings;
//     validationsGroup.messageInfos = messageInfos;

//     let iconValidaionsCell = DataViewerUtils.getIconsValidationsErrors(validationsGroup);
//     return iconValidaionsCell;
//   },
//   addIconLevelError: (validation, levelError, message) => {
//     let icon = [];
//     if (!isEmpty(validation)) {
//       icon.push(<IconTooltip levelError={levelError} message={message} style={{ width: '1.5em' }} key={levelError} />);
//     }
//     return icon;
//   }
// };
