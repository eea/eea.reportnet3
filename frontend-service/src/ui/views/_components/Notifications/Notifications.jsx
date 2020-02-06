import React, { useEffect, useContext, useRef } from 'react';

import { capitalize } from 'lodash';
import sanitizeHtml from 'sanitize-html';

import { Growl } from 'primereact/growl';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const Notifications = () => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  let growlRef = useRef();

  useEffect(() => {
    notificationContext.toShow.map(notification => {
      const message = (
        <div
          dangerouslySetInnerHTML={{
            __html: sanitizeHtml(notification.message, {
              allowedTags: ['a', 'strong'],
              allowedAttributes: {
                a: ['href', 'title']
              }
            })
          }}></div>
      );
      growlRef.current.show({
        severity: notification.type,
        summary: resourcesContext.messages[`notification${capitalize(notification.type)}Title`],
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
