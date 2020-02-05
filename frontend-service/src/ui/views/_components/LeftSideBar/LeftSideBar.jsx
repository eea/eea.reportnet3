import React, { useState, useContext } from 'react';
import { withRouter, Link } from 'react-router-dom';

import { isUndefined } from 'lodash';

import styles from './LeftSideBar.module.scss';

import { routes } from 'ui/routes';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { Icon } from 'ui/views/_components/Icon';
import { LeftSideBarButton } from './_components/LeftSideBarButton';
import { NotificationsList } from './_components/NotificationsList';

import { UserService } from 'core/services/User';

import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext';
import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { getUrl } from 'core/infrastructure/CoreUtils';

const LeftSideBar = withRouter(({ leftSideBarConfig, onToggleSideBar }) => {
  const breadCrumbContext = useContext(BreadCrumbContext);
  const notificationContext = useContext(NotificationContext);
  const leftSideBarContext = useContext(LeftSideBarContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [isNotificationVisible, setIsNotificationVisible] = useState(false);

  const renderUserProfile = () => {
    const userButtonProps = {
      href: '#userProfilePage',
      onClick: async e => {
        e.preventDefault();
      },
      title: breadCrumbContext.isLeftSideBarOpened === false ? resources.messages['userSettings'] : undefined,
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
      title: breadCrumbContext.isLeftSideBarOpened === false ? resources.messages['userSettings'] : undefined,
      icon: 'notifications',
      label: 'notifications'
    };
    return <LeftSideBarButton {...userNotificationsProps} />;
  };
  const renderButtons = () => leftSideBarContext.models.map(model => <LeftSideBarButton {...model} />);
  // leftSideBarConfig.buttons.map(button =>
  //   !button.isLink ? (
  //     <a href="#" title={breadCrumbContext.isLeftSideBarOpened === false ? button.label : undefined}>
  //       <div
  //         className={styles.leftSideBarElementWrapper}
  //         onClick={!isUndefined(button.onClick) ? () => button.onClick() : null}>
  //         <Icon icon={button.icon} className={styles.leftSideBarElementAnimation} />
  //         <span className={styles.leftSideBarText}>{button.label}</span>
  //       </div>
  //     </a>
  //   ) : (
  //     <Link
  //       to={getUrl(routes[button.linkTo.route], button.linkTo.children, button.linkTo.isRoute)}
  //       title={breadCrumbContext.isLeftSideBarOpened === false ? button.label : undefined}>
  //       <div className={styles.leftSideBarElementWrapper}>
  //         <Icon icon={button.icon} className={styles.leftSideBarElementAnimation} />
  //         <span className={styles.leftSideBarText}>{button.label}</span>
  //       </div>
  //     </Link>
  //   )

  const renderSectionButtons = () => {
    return leftSideBarContext.models.map(model => <LeftSideBarButton {...model} />);
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
      title: breadCrumbContext.isLeftSideBarOpened === false ? resources.messages['logout'] : undefined,
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
        breadCrumbContext.setMenuState();
      },
      title: breadCrumbContext.isLeftSideBarOpened === false ? resources.messages['expandSidebar'] : undefined,
      icon: breadCrumbContext.isLeftSideBarOpened ? 'angleDoubleLeft' : 'angleDoubleRight',
      label: ''
    };
    return <LeftSideBarButton {...openCloseProps} />;
  };

  return (
    <div className={`${styles.leftSideBar}${breadCrumbContext.isLeftSideBarOpened ? ` ${styles.open}` : ''}`}>
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
