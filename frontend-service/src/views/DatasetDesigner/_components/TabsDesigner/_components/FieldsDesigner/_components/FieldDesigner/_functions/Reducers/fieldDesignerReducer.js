import { TextUtils } from 'repositories/_utils/TextUtils';

export const fieldDesignerReducer = (state, { type, payload }) => {
  switch (type) {
    case 'CANCEL_SELECT_ATTACHMENT':
      return { ...state, isAttachmentEditorVisible: false };

    case 'CANCEL_SELECT_CODELIST':
      return { ...state, isCodelistEditorVisible: false };

    case 'CANCEL_SELECT_LINK':
      return { ...state, isLinkSelectorVisible: false };

    case 'RESET_FIELD':
      return {
        ...state,
        codelistItems: [],
        fieldLinkValue: null,
        fieldPkMustBeUsed: false,
        fieldPkHasMultipleValues: false,
        fieldFileProperties: { validExtensions: [], maxSize: 0 },
        referencedField: null
      };

    case 'RESET_REFERENCED_FIELD':
      return {
        ...state,
        referencedField: null,
        fieldLinkValue: null,
        fieldPkMustBeUsed: false,
        fieldPkHasMultipleValues: false
      };

    case 'RESET_NEW_FIELD':
      return {
        ...state,
        codelistItems: [],
        fieldDesignerState: '',
        fieldLinkValue: null,
        fieldPkHasMultipleValues: false,
        fieldPkMustBeUsed: false,
        fieldReadOnlyValue: false,
        fieldRequiredValue: false,
        fieldTypeValue: '',
        fieldValue: '',
        fieldDescriptionValue: '',
        fieldPKValue: false,
        fieldFileProperties: { validExtensions: [], maxSize: 0 }
      };

    case 'SET_ADD_FIELD_SENT':
      return { ...state, addFieldCallSent: payload };

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

    case 'SET_FIELD_LINK':
      return { ...state, fieldLinkValue: payload.link };

    case 'SET_LINK':
      return {
        ...state,
        fieldLinkValue: payload.link,
        fieldPkMustBeUsed: payload.pkMustBeUsed,
        fieldPkHasMultipleValues: payload.pkHasMultipleValues
      };

    case 'SET_PK':
      return { ...state, fieldPKValue: payload, fieldRequiredValue: payload ? true : state.fieldRequiredValue };

    case 'SET_PK_REFERENCED':
      return { ...state, fieldPKReferencedValue: payload };

    case 'SET_PREVIOUS_TYPE_VALUE':
      return { ...state, fieldPreviousTypeValue: payload };

    case 'SET_READONLY':
      return { ...state, fieldReadOnlyValue: payload };

    case 'SET_REQUIRED':
      return { ...state, fieldRequiredValue: payload };

    case 'SET_TYPE':
      return {
        ...state,
        fieldTypeValue: payload.type,
        codelistItems:
          !TextUtils.areEquals(payload.type.fieldType, 'MULTISELECT_CODELIST') &&
          !TextUtils.areEquals(payload.type.fieldType, 'CODELIST')
            ? []
            : state.codelistItems,
        validExtensions: !TextUtils.areEquals(payload.type.fieldType, 'ATTACHMENT') ? [] : state.validExtensions,
        fieldPreviousTypeValue: payload.previousType
      };

    case 'TOGGLE_ATTACHMENT_EDITOR_VISIBLE':
      return { ...state, isAttachmentEditorVisible: payload };

    case 'TOGGLE_CODELIST_EDITOR_VISIBLE':
      return { ...state, isCodelistEditorVisible: payload };

    case 'TOGGLE_LINK_SELECTOR_VISIBLE':
      return { ...state, isLinkSelectorVisible: payload };

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
