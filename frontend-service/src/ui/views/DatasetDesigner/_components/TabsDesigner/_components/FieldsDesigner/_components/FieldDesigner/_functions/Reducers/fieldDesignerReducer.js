import { isUndefined } from 'lodash';

export const fieldDesignerReducer = (state, { type, payload }) => {
  switch (type) {
    case 'CANCEL_SELECT_CODELIST':
      return { ...state, fieldPreviousTypeValue: payload, isCodelistEditorVisible: false };
    case 'RESET_NEW_FIELD':
      return {
        ...state,
        fieldRequiredValue: false,
        fieldDesignerState: '',
        fieldTypeValue: '',
        fieldValue: '',
        fieldDescriptionValue: ''
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
      return { ...state, fieldPKValue: payload };
    case 'SET_PREVIOUS_TYPE_VALUE':
      return { ...state, fieldPreviousTypeValue: payload };
    case 'SET_REQUIRED':
      return { ...state, fieldRequiredValue: payload };
    case 'SET_TYPE':
      return { ...state, fieldTypeValue: payload.type, fieldPreviousTypeValue: payload.previousType };
    case 'TOGGLE_CODELIST_EDITOR_VISIBLE':
      return { ...state, isCodelistEditorVisible: payload };
    case 'TOGGLE_IS_EDITING':
      return { ...state, isEditing: payload };
    case 'TOGGLE_QC_MANAGER_VISIBLE':
      return { ...state, isQCManagerVisible: payload };
    default:
      return state;
  }
};
