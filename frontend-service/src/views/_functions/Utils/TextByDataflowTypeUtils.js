import { config } from 'conf';

const getKeyByDataflowType = (dataflowType, field) => {
  switch (dataflowType) {
    case config.dataflowType.BUSINESS.value:
      return config.dataflowType.BUSINESS.labels[field];
    case config.dataflowType.CITIZEN_SCIENCE.value:
      return config.dataflowType.CITIZEN_SCIENCE.labels[field];
    case config.dataflowType.REPORTING.value:
      return config.dataflowType.REPORTING.labels[field];
    default:
      return '';
  }
};

const getLabelByDataflowType = (messages, dataflowType, field) => messages[getKeyByDataflowType(dataflowType, field)];

export const TextByDataflowTypeUtils = {
  getKeyByDataflowType,
  getLabelByDataflowType
};
