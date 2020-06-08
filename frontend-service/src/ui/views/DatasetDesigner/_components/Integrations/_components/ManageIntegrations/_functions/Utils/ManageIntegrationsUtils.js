import isEmpty from 'lodash/isEmpty';

let id = 0;

const getParameterData = (id, option, state) => {
  const selectedParameter = state.filter(parameter => parameter.id === id);
  if (!isEmpty(selectedParameter)) return selectedParameter[0][option];
};

const onAddParameter = state => ({
  id: id++,
  isEditorView: { key: false, value: false },
  key: state.parameterKey,
  value: state.parameterValue
});

const onUpdateData = (id, option, state, value) => {
  return state.map(parameter => {
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
        value: state.parameterValue
      };
    } else return parameter;
  });
};

export const ManageIntegrationsUtils = { getParameterData, onAddParameter, onUpdateData, onUpdateCompleteParameter };
