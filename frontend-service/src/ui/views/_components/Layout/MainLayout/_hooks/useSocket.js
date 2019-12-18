import React, { useContext } from 'react';

import { camelCase, isUndefined } from 'lodash';
import { Stomp } from '@stomp/stompjs';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { UserService } from 'core/services/User';

const useSocket = () => {
  const notificationContext = React.useContext(NotificationContext);
  const userContext = React.useContext(UserContext);
  const socket_url = window.env.WEBSOCKET_URL;
  React.useEffect(() => {
    console.log('userContext.socket', userContext.socket);
    if (isUndefined(userContext.socket)) {
      const token = UserService.getToken();
      const ws = new WebSocket(socket_url);
      const stompClient = Stomp.over(ws);
      userContext.onAddSocket(stompClient);

      stompClient.debug = str => {
        if (str !== '>>> PING' && str !== '<<< PONG' && str !== 'Received data') console.log('socket', str);
      };

      stompClient.connect({ token }, frame => {
        stompClient.subscribe('/user/queue/notifications', notification => {
          const { type, content } = JSON.parse(notification.body);
          notificationContext.add({ type, content });
        });
      });
    }
  }, []);
};
export { useSocket };
