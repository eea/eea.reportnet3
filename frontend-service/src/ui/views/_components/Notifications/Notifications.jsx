import React, { useState, useEffect, useContext, useRef } from 'react';

import { Growl } from 'primereact/growl';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { TextUtils } from 'ui/views/_functions/Utils';

const Notifications = ({ show = false, level, text, notificationId }) => {
  const [showNotification, setShowNotification] = useState(false);
  const notificationContext = useContext(NotificationContext);

  const onGrowlAlert = message => {
    growlRef.current.show(message);
  };
  let growlRef = useRef();

  useEffect(() => {
    notificationContext.toShow.map(notification => {
      const message = (
        <div
          dangerouslySetInnerHTML={{
            __html: notification.message
          }}></div>
      );
      growlRef.current.show({
        severity: notification.type,
        summary: notification.type,
        detail: message,
        life: notification.lifeTime,
        sticky: notification.fixed
      });
    });
    if (notificationContext.toShow.length > 0) {
      notificationContext.clearToShow();
    }
  }, [notificationContext.toShow]);

  return <Growl ref={growlRef} />;
};

export { Notifications };
