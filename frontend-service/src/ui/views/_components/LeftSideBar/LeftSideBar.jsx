import React, { Fragment, useContext, useState } from 'react';
import { withRouter } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import Joyride, { ACTIONS, EVENTS, STATUS } from 'react-joyride';

import styles from './LeftSideBar.module.scss';

import { routes } from 'ui/routes';
import { LeftSideBarButton } from './_components/LeftSideBarButton';

import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { UserService } from 'core/services/User';

import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { getUrl } from 'core/infrastructure/CoreUtils';

const LeftSideBar = withRouter(({ history, setIsNotificationVisible }) => {
  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [helpIndex, setHelpIndex] = useState();
  const [logoutConfirmVisible, setLogoutConfirmVisible] = useState(undefined);
  const [run, setRun] = useState(false);

  const handleJoyrideCallback = data => {
    const { action, status, type } = data;

    if ([ACTIONS.CLOSE].includes(action)) {
      setHelpIndex(0);
      setRun(false);
    } else {
      if ([EVENTS.STEP_AFTER, EVENTS.TARGET_NOT_FOUND].includes(type)) {
        setHelpIndex(helpIndex + (data.action === 'prev' ? -1 : 1));
      } else {
        if ([STATUS.FINISHED, STATUS.SKIPPED].includes(status)) {
          setRun(false);
        }
      }
    }
  };

  const renderHome = () => {
    const userButtonProps = {
      className: 'dataflowList-left-side-bar-home-help-step',
      href: getUrl(routes['DATAFLOWS']),
      icon: 'home',
      label: 'myDataflows',
      onClick: e => {
        e.preventDefault();
        history.push(getUrl(routes['DATAFLOWS']));
      },
      title: 'myDataflows'
    };
    return <LeftSideBarButton {...userButtonProps} />;
  };

  const renderUserProfile = () => {
    const userButtonProps = {
      className: 'dataflowList-left-side-bar-user-profile-help-step',
      href: getUrl(routes['SETTINGS']),
      icon: 'user-profile',
      label: 'userSettings',
      onClick: e => {
        e.preventDefault();
        history.push(getUrl(routes['SETTINGS']));
      },
      title: 'userSettings'
    };
    return <LeftSideBarButton {...userButtonProps} />;
  };

  const renderUserNotifications = () => {
    const userNotificationsProps = {
      buttonType: 'notifications',
      className: 'dataflowList-left-side-bar-notifications-help-step',
      href: '#',
      icon: 'notifications',
      label: 'notifications',
      onClick: async e => {
        e.preventDefault();
        setIsNotificationVisible(true);
      },
      title: 'notifications'
    };
    return <LeftSideBarButton {...userNotificationsProps} />;
  };

  const renderHelp = () => {
    const userHelpProps = {
      className: 'dataflowList-left-side-bar-help-help-step',
      href: '#',
      label: 'help',
      icon: 'questionCircle',
      onClick: async e => {
        e.preventDefault();
        setRun(true);
      },
      title: 'help'
    };
    return <LeftSideBarButton {...userHelpProps} />;
  };

  const renderSectionButtons = () => {
    return leftSideBarContext.models.map((model, i) => <LeftSideBarButton key={i} {...model} />);
  };

  const userLogout = async () => {
    userContext.socket.deactivate();
    try {
      await UserService.logout();
    } catch (error) {
      notificationContext.add({
        type: 'USER_LOGOUT_ERROR'
      });
    } finally {
      userContext.onLogout();
    }
  };

  const renderLogout = () => {
    const logoutProps = {
      href: '#',
      onClick: e => {
        e.preventDefault();
        userContext.userProps.showLogoutConfirmation ? setLogoutConfirmVisible(true) : userLogout();
      },
      title: 'logout',
      icon: 'logout',
      label: 'logout'
    };
    return <LeftSideBarButton {...logoutProps} />;
  };
  const renderOpenClose = () => {
    const openCloseProps = {
      // href: '#',
      className: 'dataflowList-left-side-bar-expand-help-step',
      icon: leftSideBarContext.isLeftSideBarOpened ? 'angleDoubleLeft' : 'angleDoubleRight',
      label: '',
      onClick: e => {
        e.preventDefault();
        leftSideBarContext.setMenuState();
      },
      title: 'expandSidebar'
    };
    return <LeftSideBarButton {...openCloseProps} />;
  };

  return (
    <Fragment>
      <Joyride
        callback={handleJoyrideCallback}
        continuous={true}
        run={run}
        scrollToFirstStep={true}
        showProgress={true}
        showSkipButton={false}
        stepIndex={helpIndex}
        steps={leftSideBarContext.steps}
        styles={{
          options: {
            primaryColor: 'var(--c-corporate-blue)',
            zIndex: 10000
          }
        }}
      />
      <div className={`${styles.leftSideBar}${leftSideBarContext.isLeftSideBarOpened ? ` ${styles.open}` : ''}`}>
        {
          <>
            <div className={`${styles.barSection} dataflowList-left-side-bar-top-section-help-step`}>
              {renderHome()}
              {renderUserProfile()}
              {renderHelp()}
              {renderUserNotifications()}
            </div>
            {!isEmpty(renderSectionButtons()) && (
              <Fragment>
                <hr />
                <div className={`${styles.barSection} dataflowList-left-side-bar-mid-section-help-step`}>
                  {renderSectionButtons()}
                </div>
              </Fragment>
            )}
            <hr />
            <div className={styles.barSection}>
              {renderLogout()}
              <div className={styles.leftSideBarElementWrapper}>{renderOpenClose()}</div>
            </div>

            {userContext.userProps.showLogoutConfirmation && logoutConfirmVisible && (
              <ConfirmDialog
                header={resources.messages['logout']}
                labelCancel={resources.messages['no']}
                labelConfirm={resources.messages['yes']}
                onConfirm={() => userLogout()}
                onHide={() => setLogoutConfirmVisible(false)}
                visible={logoutConfirmVisible}>
                {resources.messages['userLogout']}
              </ConfirmDialog>
            )}
          </>
        }
      </div>
    </Fragment>
  );
});

export { LeftSideBar };
