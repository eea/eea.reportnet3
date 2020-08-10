import { isUndefined } from 'lodash';

export const fieldDesignerReducer = (state, { type, payload }) => {
  switch (type) {
    case 'CANCEL_SELECT_ATTACHMENT':
      return {
        ...state,
        isCodelistEditorVisible: false,
        isLinkSelectorVisible: false,
        isAttachmentEditorVisible: false
      };
    case 'CANCEL_SELECT_CODELIST':
      return {
        ...state,
        isCodelistEditorVisible: false,
        isLinkSelectorVisible: false,
        isAttachmentEditorVisible: false
      };
    case 'CANCEL_SELECT_LINK':
      return {
        ...state,
        isCodelistEditorVisible: false,
        isLinkSelectorVisible: false,
        isAttachmentEditorVisible: false
      };
    case 'RESET_NEW_FIELD':
      return {
        ...state,
        codelistItems: [],
        fieldRequiredValue: false,
        fieldDesignerState: '',
        fieldLinkValue: null,
        fieldPkHasMultipleValues: false,
        fieldPkMustBeUsed: false,
        fieldTypeValue: '',
        fieldValue: '',
        fieldDescriptionValue: '',
        fieldPKValue: false
      };
    case 'SET_ATTACHMENT_PROPERTIES':
      return { ...state, fieldFileProperties: { validExtensions: payload.validExtensions, maxSize: payload.maxSize } };
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
    case 'SET_LINK':
      return { ...state, fieldLinkValue: payload };
    case 'SET_PK_MUST_BE_USED':
      return { ...state, fieldPkMustBeUsed: payload };
    case 'SET_PK_HAS_MULTIPLE_VALUES':
      return { ...state, fieldPkHasMultipleValues: payload };
    case 'SET_PK':
      return { ...state, fieldPKValue: payload, fieldRequiredValue: payload ? true : state.fieldRequiredValue };
    case 'SET_PK_REFERENCED':
      return { ...state, fieldPKReferencedValue: payload };
    case 'SET_PREVIOUS_TYPE_VALUE':
      return { ...state, fieldPreviousTypeValue: payload };
    case 'SET_REQUIRED':
      return { ...state, fieldRequiredValue: payload };
    case 'SET_TYPE':
      return {
        ...state,
        fieldTypeValue: payload.type,
        codelistItems:
          payload.type.fieldType.toUpperCase() !== 'MULTISELECT_CODELIST' &&
          payload.type.fieldType.toUpperCase() !== 'CODELIST'
            ? []
            : state.codelistItems,
        validExtensions: payload.type.fieldType.toUpperCase() !== 'ATTACHMENT' ? [] : state.validExtensions,
        fieldPreviousTypeValue: payload.previousType
      };
    case 'TOGGLE_ATTACHMENT_EDITOR_VISIBLE':
      return {
        ...state,
        isAttachmentEditorVisible: payload,
        isCodelistEditorVisible: false,
        isLinkSelectorVisible: false
      };
    case 'TOGGLE_CODELIST_EDITOR_VISIBLE':
      return {
        ...state,
        isCodelistEditorVisible: payload,
        isLinkSelectorVisible: false,
        isAttachmentEditorVisible: false
      };
    case 'TOGGLE_LINK_SELECTOR_VISIBLE':
      return {
        ...state,
        isLinkSelectorVisible: payload,
        isCodelistEditorVisible: false,
        isAttachmentEditorVisible: false
      };
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
