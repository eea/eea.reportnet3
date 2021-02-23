import React, { useContext } from 'react';

import isUndefined from 'lodash/isUndefined';
import { Stomp } from '@stomp/stompjs';

import { config } from 'conf';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { UserService } from 'core/services/User';

const useSocket = () => {
  const notificationContext = useContext(NotificationContext);
  const userContext = useContext(UserContext);
  const socket_url = window.env.WEBSOCKET_URL;
  React.useEffect(() => {
    if (isUndefined(userContext.socket)) {
      const token = UserService.getToken();
      const stompClient = Stomp.over(() => {
        return new WebSocket(socket_url);
      });
      userContext.onAddSocket(stompClient);

      stompClient.debug = () => {};
      // stompClient.debug = str => {
      //   if (str !== '>>> PING' && str !== '<<< PONG' && str !== 'Received data') console.log('socket', str);
      // };
      stompClient.reconnect_delay = 5000;
      stompClient.connect({ token }, frame => {
        stompClient.subscribe('/user/queue/notifications', notification => {
          const { type, content } = JSON.parse(notification.body);
          config.notifications.hiddenNotifications.includes(type)
            ? notificationContext.hide({ type, content })
            : notificationContext.add({ type, content });
        });
      });
    }
  }, []);
};
export { useSocket };
