import { Fragment, useContext, useState } from 'react';
import { withRouter } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import Joyride, { ACTIONS, EVENTS, STATUS } from 'react-joyride';

import styles from './LeftSideBar.module.scss';

import { routes } from 'conf/routes';
import { LeftSideBarButton } from './_components/LeftSideBarButton';

import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { UserService } from 'services/UserService';

import { LeftSideBarContext } from 'views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { getUrl } from 'repositories/_utils/UrlUtils';

const LeftSideBar = withRouter(({ history, setIsNotificationVisible, setIsSystemNotificationVisible }) => {
  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [helpIndex, setHelpIndex] = useState();
  const [logoutConfirmVisible, setLogoutConfirmVisible] = useState(undefined);
  const [run, setRun] = useState(false);

  const handleJoyrideCallback = data => {
    const { action, index, status, type } = data;

    if ([ACTIONS.CLOSE].includes(action) || [STATUS.FINISHED, STATUS.SKIPPED].includes(status)) {
      setHelpIndex(0);
      setRun(false);
    } else {
      if ([EVENTS.STEP_AFTER, EVENTS.TARGET_NOT_FOUND].includes(type)) {
        setHelpIndex(index + (action === ACTIONS.PREV ? -1 : 1));
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
      label: 'dataflows',
      onClick: e => {
        e.preventDefault();
        history.push(getUrl(routes['DATAFLOWS']));
      },
      title: 'dataflows'
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

  const renderManageSystemNotifications = () => {
    const manageSystemNotificationsProps = {
      className: 'dataflowList-left-side-bar-system-notifications-help-step',
      href: '#',
      icon: 'comment',
      label: 'systemNotifications',
      onClick: async e => {
        e.preventDefault();
        setIsSystemNotificationVisible(true);
      },
      title: 'systemNotifications'
    };
    return <LeftSideBarButton {...manageSystemNotificationsProps} />;
  };

  const renderHelp = () => {
    const userHelpProps = {
      className: 'dataflowList-left-side-bar-help-help-step',
      href: '#',
      icon: 'questionCircle',
      label: 'help',
      onClick: async e => {
        e.preventDefault();
        setRun(true);
      },
      title: 'help'
    };
    return <LeftSideBarButton {...userHelpProps} />;
  };

  const renderSectionButtons = () => {
    return leftSideBarContext.models.map(model => <LeftSideBarButton key={model.label} {...model} />);
  };

  const userLogout = async () => {
    userContext.socket.deactivate();
    try {
      await UserService.logout();
    } catch (error) {
      console.error('LeftSideBar - userLogout.', error);
      notificationContext.add({ type: 'USER_LOGOUT_ERROR' }, true);
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
        disableScrolling={true}
        run={run}
        scrollToFirstStep={true}
        showProgress={true}
        showSkipButton={false}
        stepIndex={helpIndex}
        steps={leftSideBarContext.steps}
        styles={{
          options: {
            arrowColor: 'var(--help-modal-bg)',
            backgroundColor: 'var(--help-modal-bg)',
            primaryColor: 'var(--button-primary-bg)',
            textColor: 'var(--main-font-color)',
            zIndex: 10000
          },
          buttonNext: {
            color: 'var(--button-primary-color)'
          },
          buttonBack: {
            color: 'var(--main-font-color)'
          }
        }}
      />
      <div className={`${styles.leftSideBar}${leftSideBarContext.isLeftSideBarOpened ? ` ${styles.open}` : ''}`}>
        {
          <Fragment>
            <div className={`${styles.barSection} dataflowList-left-side-bar-top-section-help-step`}>
              {renderHome()}
              {renderUserProfile()}
              {renderHelp()}
              {renderUserNotifications()}
              {/* {renderManageSystemNotifications()} */}
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
                header={resourcesContext.messages['logout']}
                labelCancel={resourcesContext.messages['no']}
                labelConfirm={resourcesContext.messages['yes']}
                onConfirm={() => userLogout()}
                onHide={() => setLogoutConfirmVisible(false)}
                visible={logoutConfirmVisible}>
                {resourcesContext.messages['userLogout']}
              </ConfirmDialog>
            )}
          </Fragment>
        }
      </div>
    </Fragment>
  );
});

export { LeftSideBar };
