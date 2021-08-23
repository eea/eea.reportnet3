import { useContext } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNull from 'lodash/isNull';

import { IconTooltip } from 'views/_components/IconTooltip';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { GroupedRecordValidationsUtils } from './_functions/Utils/GroupedRecordValidationsUtils';

export const GroupedRecordValidations = ({ parsedRecordData }) => {
  const { getGroupValidations } = GroupedRecordValidationsUtils;

  const resourcesContext = useContext(ResourcesContext);

  const addIconLevelError = (validation, levelError, message) => {
    if (isEmpty(validation)) return [];

    return [].concat(<IconTooltip key={levelError} levelError={levelError} message={message} />);
  };

  const getIconsValidationsErrors = validations => {
    if (isNull(validations)) return [];

    const blockerIcon = addIconLevelError(validations.blockers, 'BLOCKER', validations.messageBlockers);
    const errorIcon = addIconLevelError(validations.errors, 'ERROR', validations.messageErrors);
    const warningIcon = addIconLevelError(validations.warnings, 'WARNING', validations.messageWarnings);
    const infoIcon = addIconLevelError(validations.infos, 'INFO', validations.messageInfos);

    return blockerIcon.concat(errorIcon, warningIcon, infoIcon);
  };

  const validationsTemplate = () => {
    const validationsGroup = getGroupValidations(
      parsedRecordData,
      resourcesContext.messages['recordBlockers'],
      resourcesContext.messages['recordErrors'],
      resourcesContext.messages['recordWarnings'],
      resourcesContext.messages['recordInfos']
    );
    return getIconsValidationsErrors(validationsGroup);
  };

  return validationsTemplate();
};
