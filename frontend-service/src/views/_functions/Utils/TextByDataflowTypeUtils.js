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

const getLabelByDataflowType = (messages, dataflowType, field) => messages[getKeyByDataflowType(dataflowType, field)];

export const TextByDataflowTypeUtils = {
  getKeyByDataflowType,
  getLabelByDataflowType
};
