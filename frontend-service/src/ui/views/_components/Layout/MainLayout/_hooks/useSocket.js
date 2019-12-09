import React from 'react';

import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { NotificationService } from 'core/services/Notification';
import { UserService } from 'core/services/User';

import { Stomp } from '@stomp/stompjs';

const useSocket = () => {
  const notificationContext = React.useContext(NotificationContext);
  const socket_url = window.env.WEBSOCKET_URL;
  React.useEffect(() => {
    const token = UserService.getToken();
    const ws = new WebSocket(socket_url);
    // const stompClient = Stomp.client(socket_url);

    const stompClient = Stomp.over(ws);
    stompClient.connect({ token }, frame => {
      console.log('Connected: ' + frame);
      stompClient.subscribe('/user/queue/notifications', notification => {
        console.log(notification);
        // console.log(JSON.parse(notification.body));
      });
    });
  }, []);
};
export { useSocket };
