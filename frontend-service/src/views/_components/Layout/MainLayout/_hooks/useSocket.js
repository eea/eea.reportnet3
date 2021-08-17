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
      const stompClient = new Client({
        brokerURL: socket_url,
        // debug: function (str) {
        //   console.log(str);
        // },
        reconnectDelay: 1000,
        connectionTimeout: 30000,
        beforeConnect: () => {
          const token = LocalUserStorageUtils?.getTokens()?.accessToken;
          stompClient.connectHeaders = { token };
        },
        onConnect: () => {
          stompClient.subscribe('/user/queue/notifications', notification => {
            const { type, content } = JSON.parse(notification.body);
            config.notifications.hiddenNotifications.includes(type)
              ? notificationContext.hide({ type, content })
              : notificationContext.add({ type, content });
          });
        }
      });

      stompClient.activate();
      userContext.onAddSocket(stompClient);
    }
  }, []);
};
export { useSocket };
