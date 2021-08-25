import isNil from 'lodash/isNil';

const parseIntegration = data => {
  const integration = {};
  integration.description = data.integrationDescription;
  integration.externalParameters = parseIntegrationParameters(data.externalParameters);
  integration.fileExtension = data.internalParameters.fileExtension;
  integration.id = data.integrationId;
  integration.isUpdatedVisible = true;
  integration.name = data.integrationName;
  integration.operation = { label: data.operationName, value: data.operation };
  integration.processName = !isNil(data.internalParameters.processName)
    ? { label: data.internalParameters.processName, value: data.internalParameters.processName }
    : {};
  integration.repository = !isNil(data.internalParameters.repository)
    ? { label: data.internalParameters.repository, value: data.internalParameters.repository }
    : {};

  if (!isNil(data.internalParameters.notificationRequired)) {
    integration.notificationRequired = data.internalParameters.notificationRequired === 'true';
  }
  console.log({ integration });
  return integration;
};

const parseIntegrationParameters = parameters => {
  return Object.keys(parameters).map((item, index) => ({
    id: index,
    isEditorView: { key: false, value: false },
    key: item,
    prevValue: { key: '', value: '' },
    value: parameters[item]
  }));
};

export const IntegrationsUtils = { parseIntegration };
