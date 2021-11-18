export const webformFieldReducer = (state, { type, payload }) => {
  switch (type) {
    case 'SET_IS_SUBMITING':
      return { ...state, isSubmiting: payload };

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

    case 'ON_TOGGLE_DELETE_DIALOG':
      return { ...state, isDeleteAttachmentVisible: payload.value };

    case 'ON_TOGGLE_DIALOG':
      return { ...state, isFileDialogVisible: payload.value };

    case 'ON_SELECT_FIELD':
      return { ...state, selectedField: payload.field };

    case 'SET_LINK_ITEMS':
      return { ...state, linkItemsOptions: payload };

    case 'SET_SECTOR_AFFECTED':
      return { ...state, sectorAffectedValue: payload.value };

    case 'SET_INITIAL_FIELD_VALUE':
      return { ...state, initialFieldValue: payload };

    case 'SET_IS_LOADING_DATA':
      return { ...state, isLoadingData: payload };

    default:
      return state;
  }
};
