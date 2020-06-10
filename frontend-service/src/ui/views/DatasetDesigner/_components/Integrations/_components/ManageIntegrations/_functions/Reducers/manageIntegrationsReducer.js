export const manageIntegrationsReducer = (state, { type, payload }) => {
  switch (type) {
    case 'GET_UPDATED_DATA':
      return { ...state, ...payload };

    case 'MANAGE_PARAMETERS':
      return { ...state, externalParameters: payload.data, parameterKey: '', parameterValue: '' };

    case 'ON_ADD_PARAMETER':
      return {
        ...state,
        externalParameters: [...state.externalParameters, payload.data],
        parameterKey: '',
        parameterValue: ''
      };

    case 'ON_FILL':
      return { ...state, [payload.name]: payload.data };

    case 'TOGGLE_EDIT_VIEW':
      return {
        ...state,
        editorView: { isEditing: payload.isEdit, id: payload.id },
        parameterKey: payload.keyData,
        parameterValue: payload.valueData
      };

    default:
      return state;
  }
};
