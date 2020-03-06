import React, { useContext } from 'react';

import styles from './LeftSideBarButton.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const LeftSideBarButton = ({ buttonType = 'default', className, href, icon, label, onClick, style, title }) => {
  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const defaultLayout = (
    <>
      <FontAwesomeIcon
        className={`${styles.leftSideBarUserIcon} ${styles.leftSideBarElementAnimation}`}
        icon={AwesomeIcons(icon)}
      />
      <span className={styles.leftSideBarUserText}>{resourcesContext.messages[label]}</span>
    </>
  );
  const notificationsLayout = (
    <>
      <div className={`${styles.notificationIconWrapper} ${styles.leftSideBarElementAnimation}`}>
        <FontAwesomeIcon className={`${styles.leftSideBarUserIcon}`} icon={AwesomeIcons(icon)} />
        <span className={styles.notificationCounter}>{notificationContext.all.length || 0}</span>
      </div>
      <span className={styles.leftSideBarUserText}>{resourcesContext.messages[label]}</span>
    </>
  );

  const buttonsLayouts = { defaultLayout, notificationsLayout };

  return (
    <a
      className={className}
      href={href}
      onClick={onClick}
      style={style}
      title={!leftSideBarContext.isLeftSideBarOpened ? resourcesContext.messages[title] : undefined}>
      <div className={styles.leftSideBarElementWrapper}>{buttonsLayouts[`${buttonType}Layout`]}</div>
    </a>
  );
};

export { LeftSideBarButton };
