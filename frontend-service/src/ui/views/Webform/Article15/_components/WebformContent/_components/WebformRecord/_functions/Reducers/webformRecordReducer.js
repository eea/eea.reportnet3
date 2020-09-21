export const webformRecordReducer = (state, { type, payload }) => {
  switch (type) {
    case 'typeName':
      return { ...state, ...payload };

    case 'ON_FILL_FIELD':
      return {
        ...state,
        fields: {
          ...state.fields,
          [payload.option]: { ...state.fields[payload.option], newValue: payload.value }
        }
      };

    default:
      return state;
  }
};
