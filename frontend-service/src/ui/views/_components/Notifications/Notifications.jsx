import React, { useContext, useEffect, useRef, useState } from 'react';

import capitalize from 'lodash/capitalize';
import isNil from 'lodash/isNil';
import sanitizeHtml from 'sanitize-html';

import { Growl } from 'primereact/growl';
import { Button } from 'ui/views/_components/Button';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const Notifications = () => {
  const [headerHeight, setHeaderHeight] = useState(0);
  const [position, setPosition] = useState({ marginTop: '-5px' });

  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  let growlRef = useRef();

  useEffect(() => {
    const header = document.getElementById('header');

    const observer = new ResizeObserver(entries =>
      entries.forEach(entry => {
        if (headerHeight !== entry.contentRect.height) {
          setHeaderHeight(entry.contentRect.height);
        }
      })
    );

    if (!isNil(header)) {
      observer.observe(header);
    }

    return () => {
      observer.disconnect();
    };
  });

  useEffect(() => {
    recalculatePosition();
  }, [headerHeight]);

  useEffect(() => {
    notificationContext.toShow.map(notification => {
      let message;
      notification.onClick
        ? (message = (
            <div>
              {notification.message}
              <Button
                className="p-button-animated-blink "
                icon={'export'}
                onClick={() => notification.onClick()}
                label={resourcesContext.messages['downloadFile']}
              />
            </div>
          ))
        : (message = (
            <div
              dangerouslySetInnerHTML={{
                __html: sanitizeHtml(notification.message, {
                  allowedTags: ['a', 'strong'],
                  allowedAttributes: {
                    a: ['href', 'title']
                  }
                })
              }}></div>
          ));

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

  const recalculatePosition = () => {
    if (headerHeight === 180) {
      setPosition({
        marginTop: `${106}px`
      });
    }

    if (headerHeight === 70) {
      setPosition({
        marginTop: `${-5}px`
      });
    }
  };

  return <Growl ref={growlRef} style={position} />;
};

export { Notifications };
