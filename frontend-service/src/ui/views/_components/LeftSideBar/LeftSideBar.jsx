import React, { useState, useContext } from 'react';
import { withRouter } from 'react-router-dom';
import { routes } from 'ui/routes';
import styles from './LeftSideBar.module.scss';

import { LeftSideBarButton } from './_components/LeftSideBarButton';
import { NotificationsList } from './_components/NotificationsList';
import { getUrl } from 'core/infrastructure/CoreUtils';

import { UserService } from 'core/services/User';

import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext';
import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

const LeftSideBar = withRouter(({history}) => {
  const breadCrumbContext = useContext(BreadCrumbContext);
  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [isNotificationVisible, setIsNotificationVisible] = useState(false);

  const renderUserProfile = () => {
    const userButtonProps = {
      href: getUrl(routes['SETTINGS']),
      onClick: e => {

        e.preventDefault();
        history.push(getUrl(routes['SETTINGS']));
      },
      title: 'userSettings',
      icon: 'user-profile',
      label: 'userSettings'
    };
    return <LeftSideBarButton {...userButtonProps} />;
  
    
  };
  const renderUserNotifications = () => {
    const userNotificationsProps = {
      buttonType: 'notifications',
      href: '#',
      onClick: async e => {
        e.preventDefault();
        if (notificationContext.all.length > 0) setIsNotificationVisible(true);
      },
      title: 'userSettings',
      icon: 'notifications',
      label: 'notifications'
    };
    return <LeftSideBarButton {...userNotificationsProps} />;
  };

  const renderSectionButtons = () => {
    return leftSideBarContext.models.map((model, i) => <LeftSideBarButton key={i} {...model} />);
  };
  
  const renderLogout = () => {
    const logoutProps = {
      href: '#',
      onClick: async e => {
        e.preventDefault();
        userContext.socket.disconnect(() => {});
        try {
          await UserService.logout();
        } catch (error) {
          notificationContext.add({
            type: 'USER_LOGOUT_ERROR'
          });
        } finally {
          userContext.onLogout();
        }
      },
      title: 'logout',
      icon: 'logout',
      label: 'logout'
    };
    return <LeftSideBarButton {...logoutProps} />;
  };
  const renderOpenClose = () => {
    const openCloseProps = {
      href: '#',
      onClick: e => {
        e.preventDefault();
        leftSideBarContext.setMenuState();
      },
      title: 'expandSidebar',
      icon: leftSideBarContext.isLeftSideBarOpened ? 'angleDoubleLeft' : 'angleDoubleRight',
      label: ''
    };
    return <LeftSideBarButton {...openCloseProps} />;
  };

  return (
    <div className={`${styles.leftSideBar}${leftSideBarContext.isLeftSideBarOpened ? ` ${styles.open}` : ''}`}>
      {
        <>
          <div className={styles.barSection}>
            {renderUserProfile()}
            {renderUserNotifications()}
          </div>
          <hr />
          <div className={styles.barSection}>{renderSectionButtons()}</div>
          <hr />
          <div className={styles.barSection}>
            {renderLogout()}
            <div className={styles.leftSideBarElementWrapper}>{renderOpenClose()}</div>
          </div>
          <NotificationsList
            isNotificationVisible={isNotificationVisible}
            setIsNotificationVisible={setIsNotificationVisible}
          />
        </>
      }
    </div>
  );
});

export { LeftSideBar };
