import { useContext, useEffect } from 'react';

import isUndefined from 'lodash/isUndefined';
import { Client } from '@stomp/stompjs';

import { config } from 'conf';

import { LocalUserStorageUtils } from 'services/_utils/LocalUserStorageUtils';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

const useSocket = () => {
  const notificationContext = useContext(NotificationContext);
  const userContext = useContext(UserContext);
  const socket_url = window.env.WEBSOCKET_URL;

  useEffect(() => {
    if (isUndefined(userContext.socket)) {
      const maxConnectionAttempts = 10;
      let currentTry = 0;

      const stompClient = new Client({
        brokerURL: socket_url,
        reconnectDelay: 30000,
        connectionTimeout: 30000,
        beforeConnect: () => {
          currentTry++;
          const token = LocalUserStorageUtils?.getTokens()?.accessToken;
          stompClient.connectHeaders = { token };

          if (currentTry > maxConnectionAttempts) {
            notificationContext.add({ type: 'MAX_WEBSOCKET_RECONNECT_ATTEMPTS_ERROR' });
            console.error(`Exceeds max attempts (${maxConnectionAttempts}), will not try to connect now`);
            stompClient.deactivate();
          }
        },
        onConnect: () => {
          currentTry = 0;
          stompClient.subscribe('/user/queue/notifications', notification => {
            const { type, content } = JSON.parse(notification.body);
            config.notifications.hiddenNotifications.includes(type)
              ? notificationContext.hide({ type, content })
              : notificationContext.add({ type, content });
          });
          stompClient.subscribe('/user/queue/systemnotifications', notification => {
            const { type, content } = JSON.parse(notification.body);
            config.notifications.hiddenNotifications.includes(type)
              ? notificationContext.hide({ type, content })
              : notificationContext.add(JSON.parse(notification.body), false, true);
          });
        }
      });

      stompClient.activate();
      userContext.onAddSocket(stompClient);
    }
  }, []);
};
export { useSocket };
