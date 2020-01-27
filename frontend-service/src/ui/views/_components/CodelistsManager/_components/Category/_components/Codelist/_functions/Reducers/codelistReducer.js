import { capitalize } from 'lodash';

export const codelistReducer = (state, { type, payload }) => {
  switch (type) {
    case 'EDIT_CODELIST_PROPERTIES':
      return { ...state, [payload.property]: payload.value };
    case 'EDIT_CLONED_CODELIST_PROPERTIES':
      return { ...state, clonedCodelist: { ...state.clonedCodelist, [payload.property]: payload.value } };
    case 'RESET_INITIAL_VALUES':
      return { ...payload };
    case 'RESET_INITIAL_NEW_ITEM':
      return {
        ...state,
        newItem: { id: `-${state.items.length}`, code: '', label: '', definition: '', codelistId: '' }
      };
    case 'RESET_INITIAL_CLONED_CODELIST':
      return {
        ...state,
        clonedCodelist: {
          codelistId: '',
          codelistName: '',
          codelistVersion: '',
          codelistStatus: { statusType: 'design', value: 'DESIGN' },
          codelistDescription: ''
        }
      };
    case 'TOGGLE_ADD_EDIT_CODELIST_ITEM_VISIBLE':
      return { ...state, isAddEditCodelistVisible: payload.visible, formType: payload.formType };
    case 'TOGGLE_EDITING_CODELIST_ITEM':
      return { ...state, isEditing: payload };
    case 'TOGGLE_CATEGORY_CHANGED':
      return { ...state, isCategoryChanged: payload };
    case 'TOGGLE_CLONE_CODELIST_DIALOG_VISIBLE':
      return { ...state, isCloneCodelistVisible: payload };
    case 'TOGGLE_DELETE_CODELIST_ITEM_VISIBLE':
      return { ...state, isDeleteCodelistItemVisible: payload };
    case 'TOGGLE_ERROR_DIALOG_VISIBLE':
      return {
        ...state,
        error: {
          errorTitle: '',
          errorMessage: '',
          isCodelistErrorVisible: payload
        }
      };
    case 'SAVE_INITIAL_CELL_VALUE':
      return { ...state, initialCellValue: payload };
    case 'SAVE_ADDED_EDITED_ITEM':
      return { ...state, items: payload, isAddEditCodelistVisible: false };
    case 'SET_NEW_CODELIST_ITEM':
      return { ...state, newItem: { ...state.newItem, [payload.property]: payload.value } };
    case 'SET_ERRORS_DIALOG':
      return {
        ...state,
        error: {
          errorTitle: payload.errorTitle,
          errorMessage: payload.errorMessage,
          isCodelistErrorVisible: true
        }
      };
    case 'SET_ITEMS':
      return { ...state, items: payload };
    case 'SET_INITIAL_EDITED_CODELIST_ITEM':
      return { ...state, editedItem: { ...state.selectedItem } };
    case 'SET_CODELIST_DATA':
      return {
        ...state,
        codelistName: payload.name,
        codelistVersion: payload.version,
        codelistStatus: {
          statusType: capitalize(payload.status.toString().toLowerCase()),
          value: payload.status.toString().toLowerCase()
        },
        codelistDescription: payload.description,
        items: payload.items
      };
    case 'SET_EDITED_CODELIST_ITEM':
      return { ...state, editedItem: { ...state.editedItem, [payload.property]: payload.value } };
    case 'SET_SELECTED_ITEM':
      return { ...state, selectedItem: payload };
    default:
      return state;
  }
};
