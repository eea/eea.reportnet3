import { useContext, useEffect } from 'react';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';

export const useCheckNotifications = (keys, fnUseState, fnValue) => {
  const notificationContext = useContext(NotificationContext);

  useEffect(() => {
    keys.forEach(key => {
      if (notificationContext.toShow.find(notification => notification.key === key)) {
        fnUseState(fnValue);
      }
    });
  }, [notificationContext]);
};
