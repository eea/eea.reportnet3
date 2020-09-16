export const webformReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'ON_ADD_MULTIPLE_WEBFORM':
      return { ...state, multipleView: [...state.multipleView, payload.newForm] };

    case 'ON_CHANGE_TAB':
      return { ...state, isVisible: payload.isVisible };

    case 'ON_DELETE_MULTIPLE_WEBFORM':
      return { ...state, multipleView: payload.list };

    case 'ON_LOAD_DATA':
      return { ...state, data: payload.data };

    case 'ON_CHANGE_VALUE':
      return { ...state, inputData: payload.value };

    default:
      return state;
  }
};
