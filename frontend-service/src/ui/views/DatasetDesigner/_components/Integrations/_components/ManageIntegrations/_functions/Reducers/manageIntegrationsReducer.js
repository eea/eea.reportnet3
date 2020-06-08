export const manageIntegrationsReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ON_ADD_PARAMETER':
      return {
        ...state,
        externalParameters: [...state.externalParameters, payload.data],
        parameterKey: '',
        parameterValue: ''
      };

    case 'ON_EDIT_PARAMETER':
      return {
        ...state,
        editorView: { isEditing: true, id: payload.id },
        parameterKey: payload.keyData,
        parameterValue: payload.valueData
      };

    case 'ON_DELETE_PARAMETER':
      return { ...state, externalParameters: payload.data };

    case 'ON_FILL':
      return { ...state, [payload.name]: payload.data };

    case 'ON_TOGGLE_EDITOR_VIEW':
      return { ...state, externalParameters: payload.data };

    case 'ON_RESET_PARAMETER':
      return {
        ...state,
        editorView: { isEditing: false, id: null },
        parameterKey: payload.key,
        parameterValue: payload.value
      };

    case 'ON_SAVE_PARAMETER':
      return { ...state, externalParameters: payload.data, parameterKey: '', parameterValue: '' };

    case 'ON_UPDATE_PARAMETER':
      return { ...state, externalParameters: payload.data };

    default:
      return state;
  }
};
