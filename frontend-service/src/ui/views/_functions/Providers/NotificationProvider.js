import React, { useReducer } from 'react';

import { config } from 'conf';
import { routes } from 'ui/routes';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext.js';
import { NotificationService } from 'core/services/Notification';

const notificationReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ADD':
      return {
        ...state,
        toShow: [...state.toShow, payload],
        all: [...state.all, payload]
      };
    case 'READ':
      return {
        ...state,
        toShow: [...state.toShow, payload],
        all: [...state.all, payload]
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

    default:
      return state;
  }
};

const NotificationProvider = ({ children }) => {
  const [state, dispatch] = useReducer(notificationReducer, { toShow: [], all: [] });

  return (
    <NotificationContext.Provider
      value={{
        ...state,
        add: notificationDTO => {
          const notification = NotificationService.parse({
            ...notificationDTO,
            config: config.notifications,
            routes
          });
          dispatch({
            type: 'ADD',
            payload: notification
          });
        },
        read: notificationId => {
          dispatch({
            type: 'READ',
            payload: {
              notificationId
            }
          });
        },
        remove: notificationId => {
          dispatch({
            type: 'REMOVE',
            payload: {
              notificationId
            }
          });
        },
        clearToShow: () => {
          dispatch({
            type: 'CLEAR_TO_SHOW',
            payload: {}
          });
        }
      }}>
      {children}
    </NotificationContext.Provider>
  );
};
export { NotificationProvider };
