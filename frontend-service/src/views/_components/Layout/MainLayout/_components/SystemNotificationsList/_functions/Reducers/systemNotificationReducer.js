export const systemNotificationReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ON_CHANGE_PAGE':
      return { ...state, numberRows: payload.rows, firstRow: payload.first };

    case 'ON_DELETE_END':
      return { ...state, isDeleting: false, isDeleteDialogVisible: false };

    case 'ON_DELETE_START':
      return { ...state, isDeleting: true };

    case 'ON_EDIT':
      return { ...state, isVisibleCreateSysNotification: true, editNotification: payload, formType: 'EDIT' };

    case 'ON_TOGGLE_CREATE_FORM_VISIBILITY':
      return { ...state, isVisibleCreateSysNotification: payload, formType: payload ? 'CREATE' : '' };

    case 'ON_TOGGLE_DELETE_VISIBILITY':
      return { ...state, isDeleteDialogVisible: payload };

    case 'SET_SYSTEM_NOTIFICATIONS':
      return { ...state, systemNotifications: payload };

    default:
      return state;
  }
};
