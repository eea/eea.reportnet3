export const codelistReducer = (state, { type, payload }) => {
  switch (type) {
    case 'EDIT_CODELIST_PROPERTIES':
      return { ...state, [payload.property]: payload.value };
    case 'EDIT_CLONED_CODELIST_PROPERTIES':
      return { ...state, clonedCodelist: { ...state.clonedCodelist, [payload.property]: payload.value } };
    case 'RESET_INITIAL_VALUES':
      return { ...payload };
    case 'RESET_INITIAL_NEW_ITEM':
      return { ...state, newItem: { itemId: `-${state.items.length}`, code: '', label: '', definition: '' } };
    case 'RESET_INITIAL_CLONED_CODELIST':
      return {
        ...state,
        clonedCodelist: {
          codelistName: '',
          codelistVersion: '',
          codelistStatus: { statusType: 'design', value: 'DESIGN' },
          codelistDescription: ''
        }
      };
    case 'TOGGLE_ADD_EDIT_CODELIST_ITEM_VISIBLE':
      console.log(state.selectedItem);
      return { ...state, isAddEditCodelistVisible: payload.visible, formType: payload.formType };
    case 'TOGGLE_EDITING_CODELIST_ITEM':
      return { ...state, isEditing: payload };
    case 'TOGGLE_DELETE_CODELIST_ITEM_VISIBLE':
      return { ...state, isDeleteCodelistItemVisible: payload };
    case 'TOGGLE_CLONE_CODELIST_DIALOG_VISIBLE':
      return { ...state, isCloneCodelistVisible: payload };
    case 'SAVE_INITIAL_CELL_VALUE':
      return { ...state, initialCellValue: payload };
    case 'SAVE_ADDED_EDITED_ITEM':
      return { ...state, items: payload, isAddEditCodelistVisible: false };
    case 'SET_NEW_CODELIST_ITEM':
      return { ...state, newItem: { ...state.newItem, [payload.property]: payload.value } };
    case 'SET_INITIAL_EDITED_CODELIST_ITEM':
      return { ...state, editedItem: { ...state.selectedItem } };
    case 'SET_EDITED_CODELIST_ITEM':
      // const inmItems = [JSON.parse(JSON.stringify(...state.items))];
      // console.log({ inmItems });
      // const itemIdx = inmItems.map(item => item.itemId).indexOf(state.selectedItem.itemId);
      // inmItems[itemIdx][payload.property] = payload.value;
      // console.log({ inmItems });
      console.log(state.editedItem);
      return { ...state, editedItem: { ...state.editedItem, [payload.property]: payload.value } };
    case 'SET_SELECTED_ITEM':
      return { ...state, selectedItem: payload };
    default:
      return state;
  }
};
