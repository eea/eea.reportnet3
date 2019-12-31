export const codelistReducer = (state, { type, payload }) => {
  switch (type) {
    case 'EDIT_CODELIST_PROPERTIES':
      return { ...state, [payload.property]: payload.value };
    case 'RESET_INITIAL_VALUES':
      console.log(payload.items[0]);
      return { ...payload };
    case 'RESET_INITIAL_NEW_ITEM':
      return { ...state, newItem: { code: '', label: '', definition: '' } };
    case 'TOGGLE_NEW_CODELIST_VISIBLE':
      return { ...state, isNewCodelistVisible: payload };
    case 'TOGGLE_EDITING_CODELIST':
      return { ...state, isEditing: payload };
    case 'SAVE_INITIAL_CELL_VALUE':
      return { ...state, initialCellValue: payload };
    case 'SAVE_NEW_ITEM':
      return { ...state, items: payload, isNewCodelistVisible: false };
    case 'SET_NEW_CODELIST_ITEM':
      return { ...state, newItem: { ...state.newItem, [payload.property]: payload.value } };
    default:
      return state;
  }
};
