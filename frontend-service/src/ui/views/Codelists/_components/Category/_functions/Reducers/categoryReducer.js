export const categoryReducer = (state, { type, payload }) => {
  switch (type) {
    case 'SAVE_CATEGORY':
      return {
        ...state,
        categoryDescription: payload.description,
        categoryName: payload.name,
        isEditingDialogVisible: false
      };
    case 'SET_CATEGORY_INPUTS':
      console.log(payload);
      return {
        ...state,
        categoryDescription: payload.description ? payload.description : state.categoryDescription,
        categoryName: payload.name ? payload.name : state.categoryName
      };
    case 'TOGGLE_EDIT_DIALOG_VISIBLE':
      return { ...state, isEditingDialogVisible: payload };
    case 'TOGGLE_ADD_CODELIST_DIALOG_VISIBLE':
      return { ...state, isAddCodelistDialogVisible: payload };
    case 'TOGGLE_DELETE_DIALOG_VISIBLE':
      return { ...state, isDeleteConfirmDialogVisible: payload };
    case 'EDIT_NEW_CODELIST':
      return { ...state, [payload.property]: payload.value };

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
