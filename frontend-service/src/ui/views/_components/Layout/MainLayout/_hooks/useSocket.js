import React, { useContext } from 'react';

import { camelCase } from 'lodash';
import { Stomp } from '@stomp/stompjs';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';

import { UserService } from 'core/services/User';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const useSocket = () => {
  const notificationContext = React.useContext(NotificationContext);
  const socket_url = window.env.WEBSOCKET_URL;
  React.useEffect(() => {
    const token = UserService.getToken();
    const ws = new WebSocket(socket_url);
    const stompClient = Stomp.over(ws);
    stompClient.debug = str => {
      if (str !== '>>> PING' && str !== '<<< PONG' && str !== 'Received data') console.log('socket', str);
    };

    stompClient.connect({ token }, frame => {
      stompClient.subscribe('/user/queue/notifications', notification => {
        const { type, content } = JSON.parse(notification.body);
        notificationContext.add({ type, content });
      });
    });
  }, []);
};
export { useSocket };
