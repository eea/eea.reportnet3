export const notificationReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ADD':
      return {
        ...state,
        toShow: [...state.toShow, payload.notification],
        all: [...state.all, payload.notification],
        newNotification: !payload.isSystemNotification,
        newSystemNotification: payload.isSystemNotification
      };

    case 'READ':
      return {
        ...state,
        toShow: [...state.toShow, payload],
        all: [...state.all, payload],
        newNotification: false,
        newSystemNotification: false
      };

    case 'REMOVE':
      return { toShow: [...state.toShow, payload], all: [...state.all, payload] };

    case 'CLEAR_TO_SHOW':
      return { ...state, toShow: [] };

    case 'DESTROY':
      return {
        ...state,
        toShow: [],
        all: state.all.filter(notification => (!payload ? notification.isSystem : !notification.isSystem)),
        refreshedAndEnabled: payload ? false : state.refreshedAndEnabled
      };

    case 'NEW_NOTIFICATION_ADDED':
      return { ...state, newNotification: false };

    case 'NEW_SYSTEM_NOTIFICATION_ADDED':
      return { ...state, newSystemNotification: false };

    case 'HIDE_BY_KEY':
      return { ...state, hidden: payload };

    case 'HIDE':
      return { ...state, hidden: [payload] };

    case 'CLEAR_HIDDEN':
      return { ...state, hidden: [] };

    case 'REFRESHED_PAGE':
      return { ...state, refreshedAndEnabled: true };

    default:
      return state;
  }
};
