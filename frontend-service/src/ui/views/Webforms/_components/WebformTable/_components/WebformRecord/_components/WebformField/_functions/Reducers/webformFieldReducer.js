export const webformFieldReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'HANDLE_DIALOGS':
      return { ...state, isDialogVisible: { ...state.isDialogVisible, [payload.dialog]: payload.value } };

    case 'ON_FILE_DELETE_OPENED':
      return {
        ...state,
        isDeleteAttachmentVisible: true,
        selectedFieldId: payload.fieldId,
        selectedFieldSchemaId: payload.fieldSchemaId
      };

    case 'ON_FILE_UPLOAD_SET_FIELDS':
      return {
        ...state,
        selectedFieldId: payload.fieldId,
        selectedFieldSchemaId: payload.fieldSchemaId,
        selectedValidExtensions: payload.validExtensions,
        selectedMaxSize: payload.maxSize
      };

    case 'ON_FILL_FIELD':
      return {
        ...state,
        selectedField: payload.field

        // fields: {
        //   ...state.fields,
        //   [payload.option]: { ...state.fields[payload.option], newValue: payload.value }
        // }
      };

    case 'ON_TOGGLE_DELETE_DIALOG':
      return { ...state, isDeleteAttachmentVisible: payload.value };

    case 'ON_TOGGLE_DIALOG':
      return { ...state, isFileDialogVisible: payload.value };

    case 'ON_SELECT_FIELD':
      return { ...state, selectedField: payload.field };

    case 'GET_DELETE_ROW_ID':
      return { ...state, selectedRecordId: payload.selectedRecordId };

    case 'SET_IS_DELETING':
      return { ...state, isDeleting: payload.isDeleting };
    case 'SET_LINK_ITEMS':
      return { ...state, linkItemsOptions: payload };
    case 'SET_SECTOR_AFFECTED':
      return { ...state, sectorAffectedValue: payload.value };

    default:
      return state;
  }
};
