export const categoryReducer = (state, { type, payload }) => {
  switch (type) {
    case 'SAVE_CATEGORY':
      return {
        ...state,
        categoryDescription: payload.description,
        categoryShortCode: payload.shortCode,
        isEditingDialogVisible: false
      };
    case 'SET_CATEGORY_INPUTS':
      console.log(payload);
      return {
        ...state,
        categoryDescription: payload.description ? payload.description : state.categoryDescription,
        categoryId: payload.id ? payload.id : state.categoryId,
        categoryShortCode: payload.shortCode ? payload.shortCode : state.categoryShortCode
      };
    case 'SET_CODELISTS_IN_CATEGORY':
      console.log(payload.data);
      return { ...state, codelists: payload.data };
    case 'SET_ISLOADING':
      return { ...state, isLoading: payload.loading };
    case 'TOGGLE_EDIT_DIALOG_VISIBLE':
      return { ...state, isEditingDialogVisible: payload };
    case 'TOGGLE_ADD_CODELIST_DIALOG_VISIBLE':
      return { ...state, isAddCodelistDialogVisible: payload };
    case 'TOGGLE_DELETE_DIALOG_VISIBLE':
      return { ...state, isDeleteConfirmDialogVisible: payload };
    case 'TOGGLE_FILTER_DEPRECATED_CODELISTS':
      return { ...state, isFiltered: !state.isFiltered };
    case 'TOGGLE_IS_EXPANDED':
      console.log(payload);
      return { ...state, isExpanded: payload };
    case 'EDIT_NEW_CODELIST':
      return { ...state, [payload.property]: payload.value };
    case 'UPDATE_EDITING_CODELISTS':
      return { ...state, codelistsInEdition: state.codelistsInEdition + payload };
    default:
      return state;
  }
};
