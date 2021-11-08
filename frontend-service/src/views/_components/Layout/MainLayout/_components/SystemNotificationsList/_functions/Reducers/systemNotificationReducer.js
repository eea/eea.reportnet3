export const systemNotificationReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ON_ADD':
      return { ...state, isVisibleCreateSysNotification: true };

    case 'ON_TOGGLE_CREATE_FORM_VISIBILITY':
      return { ...state, isVisibleCreateSysNotification: payload };

    default:
      return state;
  }
};
