import { config } from 'conf';

const getFieldLabel = dataflowType => {
  switch (dataflowType) {
    case config.dataflowType.BUSINESS.value:
      return 'companyCode';

    case config.dataflowType.CITIZEN_SCIENCE.value:
      return 'organizationCode';

    default:
      return 'countryCode';
  }
};

const getValidationCodeButtonLabel = dataflowType => {
  switch (dataflowType) {
    case config.dataflowType.BUSINESS.value:
      return 'countryCodeAcronym';

    case config.dataflowType.CITIZEN_SCIENCE.value:
      return 'organizationCodeAcronym';

    default:
      return 'countryCodeAcronym';
  }
};

const getValidationCodeButtonTooltip = dataflowType => {
  switch (dataflowType) {
    case config.dataflowType.BUSINESS.value:
      return 'matchStringCompanyTooltip';

    case config.dataflowType.CITIZEN_SCIENCE.value:
      return 'matchStringOrganizationTooltip';

    default:
      return 'matchStringTooltip';
  }
};

const getValidationCodeKeyword = dataflowType => {
  switch (dataflowType) {
    case config.dataflowType.BUSINESS.value:
      return config.COMPANY_CODE_KEYWORD;

    case config.dataflowType.CITIZEN_SCIENCE.value:
      return config.ORGANIZATION_CODE_KEYWORD;

    default:
      return config.COUNTRY_CODE_KEYWORD;
  }
};

const getLabelByDataflowType = (messages, dataflowType, field) => {
  switch (dataflowType) {
    case config.dataflowType.BUSINESS.value:
      return messages[config.dataflowType.BUSINESS.labels[field]];

    case config.dataflowType.CITIZEN_SCIENCE.value:
      return messages[config.dataflowType.CITIZEN_SCIENCE.labels[field]];

    default:
      return messages[config.dataflowType.REPORTING.labels[field]];
  }
};

export const TextByDataflowTypeUtils = {
  getFieldLabel,
  getValidationCodeButtonLabel,
  getValidationCodeButtonTooltip,
  getValidationCodeKeyword,
  getLabelByDataflowType
};
