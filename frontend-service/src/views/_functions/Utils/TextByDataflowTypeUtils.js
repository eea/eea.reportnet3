import { config } from 'conf';

const getKeyByDataflowType = (dataflowType, field) => {
  switch (dataflowType) {
    case config.dataflowType.BUSINESS.value:
      return config.dataflowType.BUSINESS.labels[field];

    case config.dataflowType.CITIZEN_SCIENCE.value:
      return config.dataflowType.CITIZEN_SCIENCE.labels[field];

    default:
      return config.dataflowType.REPORTING.labels[field];
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
  getKeyByDataflowType,
  getLabelByDataflowType
};
