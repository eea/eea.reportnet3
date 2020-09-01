import isNil from 'lodash/isNil';

const parseIntegration = data => ({
  description: data.integrationDescription,
  externalParameters: parseIntegrationParameters(data.externalParameters),
  fileExtension: data.internalParameters.fileExtension,
  id: data.integrationId,
  isUpdatedVisible: true,
  name: data.integrationName,
  operation: { label: data.operationName, value: data.operation },
  processName: !isNil(data.internalParameters.processName)
    ? { label: data.internalParameters.processName, value: data.internalParameters.processName }
    : {},
  repository: !isNil(data.internalParameters.repository)
    ? { label: data.internalParameters.repository, value: data.internalParameters.repository }
    : {}
});

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
