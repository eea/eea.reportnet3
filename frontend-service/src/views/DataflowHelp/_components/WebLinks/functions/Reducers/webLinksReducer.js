export const webLinksReducer = (state, { type, payload }) => {
  const emptyWebLink = { id: undefined, description: '', url: '' };
  switch (type) {
    case 'ON_DELETE_END':
      return {
        ...state,
        isConfirmDeleteVisible: false,
        isDeleting: false,
        webLink: emptyWebLink
      };

    case 'ON_DELETE_START':
      return {
        ...state,
        isDeleting: true
      };

    case 'ON_DESCRIPTION_CHANGE':
      return {
        ...state,
        webLink: { ...state.webLink, description: payload.description }
      };

    case 'ON_EDIT_RECORD_END':
      return {
        ...state,
        isEditing: false,
        isSubmitting: false,
        editingId: null
      };

    case 'ON_EDIT_RECORD_START':
      return {
        ...state,
        isEditing: true,
        editingId: payload.editingId
      };

    case 'ON_HIDE_ADD_EDIT_DIALOG':
      return {
        ...state,
        errors: {
          description: { message: '', hasErrors: false },
          url: { message: '', hasErrors: false }
        },
        isAddOrEditWebLinkDialogVisible: false,
        webLink: emptyWebLink
      };

    case 'ON_HIDE_DELETE_DIALOG':
      return {
        ...state,
        isConfirmDeleteVisible: false,
        webLink: emptyWebLink
      };

    case 'ON_SAVE_RECORD':
      return {
        ...state,
        isSubmitting: true,
        webLink: payload.webLink
      };

    case 'ON_URL_CHANGE':
      return {
        ...state,
        webLink: { ...state.webLink, url: payload.url }
      };

    case 'ON_IS_PUBLIC_CHANGE':
      return {
        ...state,
        webLink: { ...state.webLink, isPublic: payload.isPublic }
      };

    case 'SET_ERRORS':
      return {
        ...state,
        errors: { ...state.errors, [payload.inputName]: payload.error }
      };

    case 'SET_IS_ADD_OR_EDIT_WEB_LINK_DIALOG_VISIBLE':
      return {
        ...state,
        isAddOrEditWebLinkDialogVisible: payload
      };

    case 'SET_IS_CONFIRM_DELETE_VISIBLE':
      return {
        ...state,
        isConfirmDeleteVisible: payload.isConfirmDeleteVisible
      };

    case 'SET_IS_SUBMITTING':
      return {
        ...state,
        isSubmitting: payload.isSubmitting
      };

    case 'SET_WEB_LINK':
      return {
        ...state,
        webLink: payload.webLink
      };

    case 'SET_WEB_LINKS_COLUMNS':
      return {
        ...state,
        webLinksColumns: payload.webLinksColumns
      };

    default:
      return state;
  }
};
