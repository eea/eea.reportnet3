export const article13Reducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'ON_TOGGLE_VIEW':
      return { ...state, isWebformView: payload.view };

    case 'ON_LOAD_PAMS_DATA':
      return {
        ...state,
        pamsRecords: payload.records,
        tableList: { ...state.tableList, single: payload.single, group: payload.group }
      };

    case 'ON_UPDATE_DATA':
      return { ...state, isDataUpdated: payload.value };

    case 'ON_SELECT_RECORD':
      return { ...state, selectedId: payload.record };

    default:
      return state;
  }
};
