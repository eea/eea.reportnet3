import isNil from 'lodash/isNil';

const parseInsideLeadReporters = (leadReporters = []) => {
  const reporters = {};
  for (let index = 0; index < leadReporters.length; index++) {
    const leadReporter = leadReporters[index];

    reporters[leadReporter.id] = leadReporter;
    reporters['empty'] = '';
  }
  return reporters;
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

export const isValidEmail = email => {
  const expression = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;

  return expression.test(email);
};
