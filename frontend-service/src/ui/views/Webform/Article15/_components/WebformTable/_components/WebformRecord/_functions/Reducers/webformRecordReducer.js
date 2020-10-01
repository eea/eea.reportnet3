export const webformRecordReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'ON_FILL_FIELD':
      return {
        ...state

        // fields: {
        //   ...state.fields,
        //   [payload.option]: { ...state.fields[payload.option], newValue: payload.value }
        // }
      };

    case 'ON_TOGGLE_DIALOG':
      return { ...state, isFileDialogVisible: payload.value };

    default:
      return state;
  }
};
