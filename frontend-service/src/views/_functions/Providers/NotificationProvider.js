import { useContext, useReducer } from 'react';

import { config } from 'conf';
import { routes } from 'conf/routes';

import camelCase from 'lodash/camelCase';

import { NotificationService } from 'services/NotificationService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { notificationReducer } from 'views/_functions/Reducers/notificationReducer';

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
        add: (notificationDTO, save = false, isSystemNotification = false) => {
          console.log(isSystemNotification);
          if (!isSystemNotification) {
            const { content, onClick, type } = notificationDTO;
            if (save) {
              NotificationService.create(notificationDTO.type, new Date(), notificationDTO.content);
            }
            const notification = NotificationService.parse({
              config: config.notifications.notificationSchema,
              content,
              date: new Date(),
              message: resourcesContext.messages[camelCase(type)],
              onClick,
              routes,
              type
            });
            dispatch({
              type: 'ADD',
              payload: { notification, isSystemNotification: false }
            });
            dispatch({
              type: 'NEW_NOTIFICATION_ADDED'
            });
          } else {
            const systemNotification = {
              id: notificationDTO.id,
              message: notificationDTO.message,
              lifeTime: notificationDTO.lifeTime,
              type: notificationDTO.level.toLowerCase(),
              fixed: true,
              isSystem: true
            };
            if (notificationDTO.enabled) {
              dispatch({
                type: 'ADD',
                payload: { notification: systemNotification, isSystemNotification: true }
              });
              dispatch({
                type: 'NEW_SYSTEM_NOTIFICATION_ADDED'
              });
            }
          }
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

        deleteAll: (isSystemNotification = false) => {
          dispatch({
            type: 'DESTROY',
            payload: isSystemNotification
          });
        },

        clearHiddenNotifications: () => dispatch({ type: 'CLEAR_HIDDEN' }),

        removeHiddenByKey: key => {
          dispatch({ type: 'HIDE_BY_KEY', payload: state.hidden.filter(notification => notification.key !== key) });
        },

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
