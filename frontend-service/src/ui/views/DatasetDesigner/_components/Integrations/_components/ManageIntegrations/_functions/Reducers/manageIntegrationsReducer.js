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

    case 'ON_EDIT_PARAMETER':
      return {
        ...state,
        editorView: { isEditing: true, id: payload.id },
        parameterKey: payload.keyData,
        parameterValue: payload.valueData
      };

    case 'ON_FILL':
      return { ...state, [payload.name]: payload.data };

    case 'ON_RESET_PARAMETER':
      return {
        ...state,
        editorView: { isEditing: false, id: null },
        parameterKey: payload.key,
        parameterValue: payload.value
      };

    default:
      return state;
  }
};
