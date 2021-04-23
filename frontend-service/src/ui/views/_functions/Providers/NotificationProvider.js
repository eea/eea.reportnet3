import { useContext, useReducer } from 'react';

import { config } from 'conf';
import { routes } from 'ui/routes';

import camelCase from 'lodash/camelCase';

import { NotificationService } from 'core/services/Notification';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext.js';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { notificationReducer } from 'ui/views/_functions/Reducers/notificationReducer';

const NotificationProvider = ({ children }) => {
  const resourcesContext = useContext(ResourcesContext);

  const [state, dispatch] = useReducer(notificationReducer, {
    all: [],
    hidden: [],
    newNotification: false,
    toShow: []
  });

  return (
    <NotificationContext.Provider
      value={{
        ...state,
        add: notificationDTO => {
          const { content, onClick, type } = notificationDTO;
          const notification = NotificationService.parse({
            config: config.notifications.notificationSchema,
            content,
            message: resourcesContext.messages[camelCase(type)],
            onClick,
            routes,
            type
          });

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

        clearHiddenNotifications: () => dispatch({ type: 'CLEAR_HIDDEN' }),

        hide: notificationDTO => {
          const { type, content } = notificationDTO;

          const notification = NotificationService.parseHidden({
            type,
            content,
            config: config.notifications.hiddenNotifications
          });

          dispatch({ type: 'HIDE', payload: notification });
        }
      }}>
      {children}
    </NotificationContext.Provider>
  );
};
export { NotificationProvider };
