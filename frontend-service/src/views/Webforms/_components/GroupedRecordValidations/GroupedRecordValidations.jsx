import { useContext } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNull from 'lodash/isNull';

import { IconTooltip } from 'views/_components/IconTooltip';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { GroupedRecordValidationsUtils } from './_functions/Utils/GroupedRecordValidationsUtils';

export const GroupedRecordValidations = ({ parsedRecordData }) => {
  const { getGroupValidations } = GroupedRecordValidationsUtils;

  const resources = useContext(ResourcesContext);

  const addIconLevelError = (validation, levelError, message) => {
    let icon = [];
    if (!isEmpty(validation)) icon.push(<IconTooltip key={levelError} levelError={levelError} message={message} />);

    return icon;
  };

  const getIconsValidationsErrors = validations => {
    let icons = [];
    if (isNull(validations)) return icons;

    const blockerIcon = addIconLevelError(validations.blockers, 'BLOCKER', validations.messageBlockers);
    const errorIcon = addIconLevelError(validations.errors, 'ERROR', validations.messageErrors);
    const warningIcon = addIconLevelError(validations.warnings, 'WARNING', validations.messageWarnings);
    const infoIcon = addIconLevelError(validations.infos, 'INFO', validations.messageInfos);

    icons = blockerIcon.concat(errorIcon, warningIcon, infoIcon);
    return icons;
  };

  const validationsTemplate = () => {
    const validationsGroup = getGroupValidations(
      parsedRecordData,
      resources.messages['recordBlockers'],
      resources.messages['recordErrors'],
      resources.messages['recordWarnings'],
      resources.messages['recordInfos']
    );
    return getIconsValidationsErrors(validationsGroup);
  };

  return validationsTemplate();
};
