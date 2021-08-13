import isNil from 'lodash/isNil';

const getSectionValidationRedirectionUrl = sectionDTO => {
  if (!isNil(sectionDTO)) {
    if (sectionDTO === 'REPORTING') {
      return 'DATASET';
    }

    if (sectionDTO === 'DESIGN') {
      return 'DATASET_SCHEMA';
    }

    return 'EU_DATASET';
  }
};

export const NotificationUtils = {
  getSectionValidationRedirectionUrl
};
