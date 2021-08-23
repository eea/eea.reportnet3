import { Fragment, useContext, useEffect, useState } from 'react';

import styles from './LeftSideBarButton.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import ReactTooltip from 'react-tooltip';

import { LeftSideBarContext } from 'views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

const LeftSideBarButton = ({ buttonType = 'default', className, href, icon, label, onClick, style, title }) => {
  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [animate, setAnimate] = useState(false);

  useEffect(() => {
    let isMounted = true;
    if (notificationContext.newNotification) {
      setAnimate(true);
    } else {
      setTimeout(() => {
        if (isMounted) setAnimate(false);
      }, 600);
    }
    return () => {
      isMounted = false;
    };
  }, [notificationContext.newNotification]);

  const defaultLayout = (
    <Fragment>
      <FontAwesomeIcon
        aria-label={title}
        className={`${styles.leftSideBarUserIcon} ${styles.leftSideBarElementAnimation}`}
        icon={AwesomeIcons(icon)}
        role="button"
      />
      <span className={styles.leftSideBarUserText}>{resourcesContext.messages[label]}</span>
    </Fragment>
  );
  const notificationsLayout = (
    <Fragment>
      <div className={`${styles.notificationIconWrapper} ${styles.leftSideBarElementAnimation}`}>
        <FontAwesomeIcon
          aria-label={resourcesContext.messages['notifications']}
          className={`${styles.leftSideBarUserIcon} ${animate ? styles.leftSideBarElementNotification : ''}`}
          icon={AwesomeIcons(icon)}
          role="button"
        />

        {notificationContext.all.length > 0 && (
          <span className={styles.notificationCounter}>{notificationContext.all.length || 0}</span>
        )}
      </div>
      <span className={styles.leftSideBarUserText}>{resourcesContext.messages[label]}</span>
    </Fragment>
  );

  const buttonsLayouts = { defaultLayout, notificationsLayout };

  return (
    <Fragment>
      <a className={className} data-for={title} data-tip href={href} onClick={onClick} style={style}>
        <div className={styles.leftSideBarElementWrapper}>{buttonsLayouts[`${buttonType}Layout`]}</div>
      </a>
      {!leftSideBarContext.isLeftSideBarOpened ? (
        <ReactTooltip border={true} className={styles.tooltipClass} effect="solid" id={title} place="right">
          <span>{!leftSideBarContext.isLeftSideBarOpened ? resourcesContext.messages[title] : undefined}</span>
        </ReactTooltip>
      ) : null}
    </Fragment>
  );
};

export { LeftSideBarButton };
