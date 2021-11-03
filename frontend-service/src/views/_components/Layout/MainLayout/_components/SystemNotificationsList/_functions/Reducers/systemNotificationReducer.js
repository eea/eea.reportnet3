export const systemNotificationReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ON_EDIT':
      return { ...state, isVisibleCreateSysNotification: true, editNotification: payload, formType: 'EDIT' };

    case 'ON_TOGGLE_CREATE_FORM_VISIBILITY':
      return { ...state, isVisibleCreateSysNotification: payload };

    default:
      return state;
  }
};
