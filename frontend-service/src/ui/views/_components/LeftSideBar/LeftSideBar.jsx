import React, { useState, useContext } from 'react';
import { withRouter } from 'react-router-dom';
import Joyride, { STATUS } from 'react-joyride';

import styles from './LeftSideBar.module.scss';

import { LeftSideBarButton } from './_components/LeftSideBarButton';
import { NotificationsList } from './_components/NotificationsList';

import { UserService } from 'core/services/User';

import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext';
import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

const LeftSideBar = withRouter(() => {
  const breadCrumbContext = useContext(BreadCrumbContext);
  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [isNotificationVisible, setIsNotificationVisible] = useState(false);
  const [run, setRun] = useState(false);

  const handleJoyrideCallback = data => {
    const { status, type } = data;
    const finishedStatuses = [STATUS.FINISHED, STATUS.SKIPPED];

    if (finishedStatuses.includes(status)) {
      setRun(false);
    }
  };

  const renderUserProfile = () => {
    const userButtonProps = {
      href: '#userProfilePage',
      onClick: async e => {
        e.preventDefault();
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

  const renderHelp = () => {
    const userHelpProps = {
      href: '#',
      onClick: async e => {
        e.preventDefault();
        setRun(true);
      },
      title: leftSideBarContext.helpTitle,
      icon: 'questionCircle',
      label: leftSideBarContext.helpTitle
    };
    return <LeftSideBarButton {...userHelpProps} />;
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
    <>
      <Joyride
        callback={handleJoyrideCallback}
        continuous={true}
        run={run}
        scrollToFirstStep={true}
        showProgress={true}
        showSkipButton={true}
        steps={leftSideBarContext.steps}
        styles={{
          options: {
            zIndex: 10000
          }
        }}
      />
      <div className={`${styles.leftSideBar}${leftSideBarContext.isLeftSideBarOpened ? ` ${styles.open}` : ''}`}>
        {
          <>
            <div className={styles.barSection}>
              {renderUserProfile()}
              {renderUserNotifications()}
              {renderHelp()}
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
    </>
  );
});

export { LeftSideBar };
