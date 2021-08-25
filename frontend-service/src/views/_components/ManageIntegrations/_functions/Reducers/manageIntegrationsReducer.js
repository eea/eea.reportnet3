export const manageIntegrationsReducer = (state, { type, payload }) => {
  switch (type) {
    case 'CLEAR_FILE_EXTENSION_NOTIFICATION_REQUIRED':
      return {
        ...state,
        fileExtension: payload.fileExtension,
        notificationRequired: payload.notificationRequired
      };

    case 'GET_PROCESSES':
      return { ...state, processes: payload.data };

    case 'GET_REPOSITORIES':
      return { ...state, repositories: payload.data };

    case 'GET_CLONED_AND_UPDATED_DATA':
      return { ...state, ...payload };

    case 'IS_LOADING':
      return { ...state, isLoading: payload.value };

    case 'IS_NOTIFICATION_REQUIRED':
      return { ...state, [payload.name]: payload.data };

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

    case 'ON_FILL_OPERATION':
      return { ...state, [payload.name]: payload.data };

    case 'ON_FILL_REPOSITORY':
      return { ...state, [payload.name]: payload.data, processName: payload.processName };

    case 'SET_IS_INTEGRATION_MANAGING':
      return { ...state, [payload.state]: payload.value };

    case 'SHOW_ERRORS':
      return { ...state, displayErrors: payload.value };

    case 'TOGGLE_EDIT_VIEW':
      return {
        ...state,
        editorView: { isEditing: payload.isEdit, id: payload.id },
        parameterKey: payload.keyData,
        parameterValue: payload.valueData
      };

    case 'TOGGLE_ERROR_DIALOG':
      return {
        ...state,
        parametersErrors: {
          ...state.parametersErrors,
          content: payload.content,
          header: payload.header,
          isDialogVisible: payload.value,
          option: payload.option
        }
      };

    default:
      return state;
  }
};
