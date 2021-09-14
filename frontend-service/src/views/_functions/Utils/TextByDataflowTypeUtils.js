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

export const TextByDataflowTypeUtils = {
  getFieldLabel,
  getValidationCodeButtonLabel,
  getValidationCodeButtonTooltip,
  getValidationCodeKeyword
};
