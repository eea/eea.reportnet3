import isEmpty from 'lodash/isEmpty';
import isEqual from 'lodash/isEqual';

const getParameterData = (id, option, parameters) => {
  const selectedParameter = parameters.filter(parameter => parameter.id === id);
  if (!isEmpty(selectedParameter)) return selectedParameter[0][option];
};

const isDuplicatedIntegration = (integration, incomingIntegration) => {
  const currentIntegration = {
    description: integration.description,
    externalParameters: integration.externalParameters,
    fileExtension: integration.fileExtension,
    id: integration.id,
    isUpdatedVisible: integration.isUpdatedVisible,
    name: integration.name,
    operation: integration.operation,
    processName: integration.processName,
    repository: integration.repository
  };

  return isEqual([currentIntegration].sort(), [incomingIntegration].sort());
};

const isDuplicatedIntegrationName = (currentName, integrationsList = [], id) => {
  const names = integrationsList
    .filter(integration => integration.integrationId !== id)
    .map(integration => integration.integrationName.toLowerCase());
  return names.includes(currentName.toLowerCase().trim());
};

const isDuplicatedParameter = (id, parameters, value) => {
  return parameters
    .filter(parameter => parameter.id !== id)
    .map(parameter => parameter.key)
    .includes(value);
};

const isFormEmpty = state => {
  const requiredFields = requiredKeys(state);
  const isEmptyForm = [];
  for (let index = 0; index < requiredFields.length; index++) {
    const key = requiredFields[index];
    isEmptyForm.push(isEmpty(state[key]));
  }

  return isEmptyForm.includes(true);
};

const isParameterEditing = parameters => {
  const isEditorView = parameters
    .map(parameter => parameter.isEditorView)
    .map(editor => Object.values(editor))
    .flat();

  return isEditorView.includes(true);
};

const onAddParameter = state => {
  let id = state.externalParameters.length;

  return {
    id: id++,
    isEditorView: { key: false, value: false },
    key: state.parameterKey,
    prevValue: { key: '', value: '' },
    value: state.parameterValue
  };
};

const onUpdateData = (id, option, parameters, value) => {
  return parameters.map(parameter => {
    if (parameter.id === id) {
      Object.assign({}, parameter, (parameter[option] = value));
      return parameter;
    } else return parameter;
  });
};

const onUpdateCompleteParameter = (id, state) => {
  return state.externalParameters.map(parameter => {
    if (parameter.id === id) {
      return {
        id: id,
        isEditorView: { key: false, value: false },
        key: state.parameterKey,
        prevValue: { key: '', value: '' },
        value: state.parameterValue
      };
    } else return parameter;
  });
};

const printError = (field, state) => {
  const requiredFields = requiredKeys(state);

  return state.displayErrors && requiredFields.includes(field) && isEmpty(state[field]) ? 'error' : undefined;
};

const requiredKeys = state => {
  const requiredFields = ['name', 'operation', 'repository', 'processName'];

  if (!isEmpty(state.operation) && (state.operation.value === 'EXPORT' || state.operation.value === 'IMPORT')) {
    requiredFields.push('fileExtension');
  }

  return requiredFields;
};

const toggleParameterEditorView = (id, option, parameters) => {
  return parameters.map(parameter => {
    if (parameter.id === id) {
      Object.assign({}, parameter, (parameter.isEditorView[option] = !parameter.isEditorView[option]));
      if (parameter.isEditorView[option]) {
        parameter.prevValue = { ...parameter.prevValue, [option]: parameter[option] };
      }
      return parameter;
    } else return parameter;
  });
};

export const ManageIntegrationsUtils = {
  getParameterData,
  isDuplicatedIntegration,
  isDuplicatedIntegrationName,
  isDuplicatedParameter,
  isFormEmpty,
  isParameterEditing,
  onAddParameter,
  onUpdateCompleteParameter,
  onUpdateData,
  printError,
  toggleParameterEditorView
};
