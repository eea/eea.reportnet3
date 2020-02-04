import React, { useContext } from 'react';

import styles from './LeftSideBarButton.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const LeftSideBarButton = ({ buttonType = 'default', href, onClick, title, icon, label }) => {
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
  console.log('buttonsLayouts', buttonsLayouts[`${buttonType}Layout`]);

  return (
    <a href={href} onClick={onClick} title={title}>
      <div className={styles.leftSideBarElementWrapper}>{buttonsLayouts[`${buttonType}Layout`]}</div>
    </a>
  );
};

export { LeftSideBarButton };
