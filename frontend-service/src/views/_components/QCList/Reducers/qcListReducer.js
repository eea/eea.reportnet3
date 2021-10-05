export const qcListReducer = (state, { type, payload }) => {
  switch (type) {
    case 'FILTER_DATA':
      return { ...state, filteredData: payload.data };

    case 'IS_DATA_UPDATED':
      return { ...state, isDataUpdated: payload.value };

    case 'IS_DELETE_DIALOG_VISIBLE':
      return { ...state, isDeleteDialogVisible: payload.value };

    case 'IS_FILTERED':
      return { ...state, filtered: payload.value };

    case 'IS_LOADING':
      return { ...state, isLoading: payload.value };

    case 'ON_LOAD_VALIDATION_ID':
      return { ...state, validationId: payload.value };

    case 'ON_LOAD_VALIDATION_LIST':
      return { ...state, validationList: payload.validationsServiceList };

    case 'RESET_FILTERED_DATA':
      return {
        ...state,
        filteredData: state.initialFilteredData,
        // initialFilteredData: [],
        validationList: { ...state.validationList, validations: state.initialValidationsList },
        editingRows: state.editingRows - 1
      };

    case 'SET_INITIAL_DATA':
      return {
        ...state,
        initialFilteredData: state.filteredData,
        initialValidationsList: state.validationList.validations,
        editingRows: state.editingRows + 1
      };

    case 'SET_DELETED_RULE_ID':
      return { ...state, deletedRuleId: payload.deletedRuleId };

    case 'UPDATE_FILTER_DATA_AND_VALIDATIONS':
      return {
        ...state,
        filteredData: payload,
        validationList: { ...state.validationList, validations: payload }
      };

    case 'UPDATE_EDITING_ROWS_COUNT':
      console.log({ payload });
      return { ...state, editingRows: payload };

    default: {
      return state;
    }
  }
};
