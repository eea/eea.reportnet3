import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { DownloadFile } from 'ui/views/_components/DownloadFile';

import { RegularExpressions } from 'ui/views/_functions/Utils/RegularExpressions';

import { RepresentativeService } from 'core/services/Representative';

const parseInsideLeadReporters = (leadReporters = []) => {
  const reporters = {};
  for (let index = 0; index < leadReporters.length; index++) {
    const leadReporter = leadReporters[index];

    reporters[leadReporter.id] = leadReporter;
    reporters['empty'] = '';
  }
  return reporters;
};

export const onExportLeadReportersTemplate = async selectedDataProviderGroup => {
  const response = await RepresentativeService.downloadTemplateById(selectedDataProviderGroup.dataProviderGroupId);
  if (!isNil(response)) {
    DownloadFile(response, `GroupId_${selectedDataProviderGroup.dataProviderGroupId}_Template.csv`);
  }
};

export const parseLeadReporters = (representatives = []) => {
  const filteredRepresentatives = representatives.filter(re => !isNil(re.dataProviderId));

  const dataProvidersLeadReporters = {};

  filteredRepresentatives.forEach(representative => {
    if (isNil(representative.leadReporters)) return {};

    dataProvidersLeadReporters[representative.dataProviderId] = parseInsideLeadReporters(representative.leadReporters);
  });

  return dataProvidersLeadReporters;
};

export const isDuplicatedLeadReporter = (inputValue, dataProviderId, leadReporters) => {
  if (isEmpty(leadReporters)) return false;

  const existingLeadReporters = Object.values(leadReporters[dataProviderId])
    .map(reporter => reporter.account)
    .filter(reporter => !isNil(reporter));

  return existingLeadReporters.includes(inputValue);
};

export const isValidEmail = email => {
  return RegularExpressions['email'].test(email);
};
