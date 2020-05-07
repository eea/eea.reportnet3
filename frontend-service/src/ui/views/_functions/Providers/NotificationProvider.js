import React, { useReducer, useContext } from 'react';

import { config } from 'conf';
import { routes } from 'ui/routes';

import { camelCase } from 'lodash';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext.js';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { NotificationService } from 'core/services/Notification';

const notificationReducer = (state, { type, payload }) => {
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
      return { ...state, hidden: [...state.hidden, payload.hidden] };

    default:
      return state;
  }
};

const NotificationProvider = ({ children }) => {
  const [state, dispatch] = useReducer(notificationReducer, {
    all: [],
    hidden: [],
    newNotification: false,
    toShow: []
  });
  const resourcesContext = useContext(ResourcesContext);

  return (
    <NotificationContext.Provider
      value={{
        ...state,
        add: notificationDTO => {
          const { type, content } = notificationDTO;
          const notification = NotificationService.parse({
            type,
            content,
            message: resourcesContext.messages[camelCase(type)],
            config: config.notifications.notificationSchema,
            routes
          });
          // console.log('notification', notification);
          dispatch({
            type: 'ADD',
            payload: notification
          });
          dispatch({
            type: 'NEW_NOTIFICATION_ADDED'
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
        removeById: notificationId => {
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
        },
        deleteAll: () => {
          dispatch({
            type: 'DESTROY'
          });
        },
        hide: notification => dispatch({ type: 'HIDE', payload: { hidden: notification.type } })
      }}>
      {children}
    </NotificationContext.Provider>
  );
};
export { NotificationProvider };
