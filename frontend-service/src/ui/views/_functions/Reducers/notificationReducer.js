export const notificationReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ADD':
      return {
        ...state,
        toShow: [...state.toShow, payload],
        all: [...state.all, payload],
        newNotification: true
      };

    case 'READ':
      return {
        ...state,
        toShow: [...state.toShow, payload],
        all: [...state.all, payload],
        newNotification: false
      };

    case 'REMOVE':
      return {
        toShow: [...state.toShow, payload],
        all: [...state.all, payload]
      };

    case 'CLEAR_TO_SHOW':
      return {
        ...state,
        toShow: []
      };

    case 'DESTROY':
      return {
        ...state,
        toShow: [],
        all: []
      };

    case 'NEW_NOTIFICATION_ADDED':
      return {
        ...state,
        newNotification: false
      };

    case 'HIDE':
      return { ...state, hidden: [payload] };

    case 'CLEAR_HIDDEN':
      return { ...state, hidden: [] };

    default:
      return state;
  }
};
