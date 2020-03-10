import { isUndefined } from 'lodash';

export const fieldDesignerReducer = (state, { type, payload }) => {
  switch (type) {
    case 'CANCEL_SELECT_CODELIST':
      return { ...state, fieldPreviousTypeValue: payload, isFKSelectorVisible: false };
    case 'RESET_NEW_FIELD':
      return {
        ...state,
        fieldRequiredValue: false,
        fieldDesignerState: '',
        fieldTypeValue: '',
        fieldValue: '',
        fieldDescriptionValue: '',
        fieldIsPKValue: ''
      };
    case 'SET_CODELIST_ITEMS':
      return { ...state, codelistItems: payload };
    case 'SET_DESCRIPTION':
      return { ...state, fieldDescriptionValue: payload };
    case 'SET_INITIAL_FIELD_VALUE':
      return { ...state, initialFieldValue: payload };
    case 'SET_INITIAL_FIELD_DESCRIPTION':
      return { ...state, initialFieldValue: payload };
    case 'SET_NAME':
      return { ...state, fieldValue: payload };
    case 'SET_PK':
      return { ...state, fieldIsPKValue: payload, fieldRequiredValue: payload ? true : state.fieldRequiredValue };
    case 'SET_PREVIOUS_TYPE_VALUE':
      return { ...state, fieldPreviousTypeValue: payload };
    case 'SET_REQUIRED':
      return { ...state, fieldRequiredValue: payload };
    case 'SET_TYPE':
      return { ...state, fieldTypeValue: payload.type, fieldPreviousTypeValue: payload.previousType };
    case 'TOGGLE_CODELIST_EDITOR_VISIBLE':
      return { ...state, isCodelistEditorVisible: payload, isFKSelectorVisible: false };
    case 'TOGGLE_FK_SELECTOR_VISIBLE':
      return { ...state, isFKSelectorVisible: payload, isCodelistEditorVisible: false };
    case 'TOGGLE_IS_DRAGGING':
      return { ...state, isDragging: payload };
    case 'TOGGLE_IS_EDITING':
      return { ...state, isEditing: payload };
    case 'TOGGLE_QC_MANAGER_VISIBLE':
      return { ...state, isQCManagerVisible: payload };
    default:
      return state;
  }
};
