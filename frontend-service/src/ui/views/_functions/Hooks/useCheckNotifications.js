import { useContext, useEffect } from 'react';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';

export const useCheckNotifications = (keys, fnUseState, fnValue) => {
  const notificationContext = useContext(NotificationContext);

  useEffect(() => {
    keys.forEach(key => {
      const response = notificationContext.toShow.find(notification => notification.key === key);
      if (response) {
        fnUseState(fnValue);
      }
    });
  }, [notificationContext]);
};
