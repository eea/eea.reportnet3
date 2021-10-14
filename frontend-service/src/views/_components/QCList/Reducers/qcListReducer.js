import cloneDeep from 'lodash/cloneDeep';
export const qcListReducer = (state, { type, payload }) => {
  switch (type) {
    case 'FILTER_DATA':
      return { ...state, filteredData: payload.data, editingRows: payload.data.length === 0 ? [] : state.editingRows };

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
        filteredData: cloneDeep(state.initialFilteredData),
        validationList: { ...state.validationList, validations: cloneDeep(state.initialValidationsList) },
        editingRows: state.editingRows.filter(editingRow => editingRow.id !== payload.id)
      };

    case 'SET_INITIAL_DATA':
      return {
        ...state,
        initialFilteredData: cloneDeep(state.filteredData),
        initialValidationsList: cloneDeep(state.validationList.validations),
        editingRows: [...state.editingRows, payload]
      };

    case 'SET_DELETED_RULE_ID':
      return { ...state, deletedRuleId: payload.deletedRuleId };

    case 'UPDATE_FILTER_DATA_AND_VALIDATIONS':
      return {
        ...state,
        filteredData: cloneDeep(payload.qcs),
        editingRows: payload.editRows,
        validationList: { ...state.validationList, validations: cloneDeep(payload.qcs) }
      };

    case 'UPDATE_VALIDATION_RULE':
      return { ...state, editingRows: state.editingRows.filter(editingRow => editingRow.id !== payload.id) };

    default: {
      return state;
    }
  }
};
