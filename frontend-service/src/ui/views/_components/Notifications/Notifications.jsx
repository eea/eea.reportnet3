import React, { useContext, useEffect, useRef, useState } from 'react';

import capitalize from 'lodash/capitalize';
import isNil from 'lodash/isNil';
import DOMPurify from 'dompurify';

import styles from './Notifications.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Growl } from 'primereact/growl';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

const Notifications = () => {
  const [headerHeight, setHeaderHeight] = useState(0);
  const [position, setPosition] = useState({ marginTop: '-5px' });

  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

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
                className={`p-button-animated-blink ${styles.downloadButton}`}
                icon={'export'}
                onClick={() => notification.onClick()}
                label={resourcesContext.messages['downloadFile']}
              />
            </div>
          ))
        : (message = (
            <div
              dangerouslySetInnerHTML={{
                __html: DOMPurify.sanitize(notification.message, {
                  ALLOWED_TAGS: ['a', 'strong'],
                  ALLOWED_ATTR: ['href', 'title']
                })
              }}></div>
          ));

      if (userContext.userProps.notificationSound) {
        playNotificationSound(440.0, 'sine');
      }

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

  const playNotificationSound = (frequency, type) => {
    var context = new AudioContext();
    var o = context.createOscillator();
    var g = context.createGain();
    o.type = type;
    o.connect(g);
    o.frequency.value = frequency;
    g.connect(context.destination);
    o.start(0);
    g.gain.exponentialRampToValueAtTime(0.00001, context.currentTime + 1);
  };

  const recalculatePosition = () => {
    if (headerHeight === 180) {
      setPosition({
        marginTop: `${106}px`
      });
    }

    if (headerHeight === 64) {
      setPosition({
        marginTop: `${-5}px`
      });
    }
  };

  return <Growl ref={growlRef} style={position} />;
};

export { Notifications };
