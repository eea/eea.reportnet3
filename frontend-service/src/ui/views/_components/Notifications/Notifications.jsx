import React, { useState, useEffect, useContext, useRef } from 'react';

import { Growl } from 'primereact/growl';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';

const Notifications = ({ show = false, level, text, notificationId }) => {
  const [showNotification, setShowNotification] = useState(false);
  const notificationContext = useContext(NotificationContext);

  const onGrowlAlert = message => {
    growlRef.current.show(message);
  };
  let growlRef = useRef();

  useEffect(() => {
    notificationContext.toShow.map(notification => {
      growlRef.current.show({
        severity: notification.type,
        summary: notification.type,
        detail: notification.message,
        life: notification.lifeTime,
        sticky: notification.fixed ? true : false
      });
    });
    if (notificationContext.toShow.length > 0) {
      notificationContext.clearToShow();
    }
  }, [notificationContext.toShow]);

  return <Growl ref={growlRef} />;
};

export { Notifications };
