import React, { useState, useContext } from 'react';
import { withRouter, Link } from 'react-router-dom';

import { isUndefined } from 'lodash';

import styles from './LeftSideBar.module.css';

import { routes } from 'ui/routes';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'ui/views/_components/Button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { Icon } from 'ui/views/_components/Icon';

import logo from 'assets/images/EEA_agency_logo.svg';

import { UserService } from 'core/services/User';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { getUrl } from 'core/infrastructure/CoreUtils';

const LeftSideBar = withRouter(({ leftSideBarConfig, onToggleSideBar }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const setVisibleHandler = (fnUseState, visible) => {
    fnUseState(visible);
  };

  const renderTitle = () => (
    <a
      href={getUrl(routes.DATAFLOWS)}
      className={styles.appLogo}
      title={resources.messages['titleHeader']}
      onClick={e => {
        e.preventDefault();
        //history.push(getUrl(routes.DATAFLOWS));
      }}>
      <div className={styles.leftSideBarElementWrapper}>
        <img height="30px" src={logo} alt={resources.messages['titleHeader']} className={styles.leftSideBarLogo} />
        <span className={styles.leftSideBarTextTitle}>{resources.messages['titleHeader']}</span>
      </div>
    </a>
  );

  const renderUserProfile = () => (
    <a
      href="#userProfilePage"
      onClick={async e => {
        e.preventDefault();
      }}
      title={resources.messages['userSettings']}>
      <div className={styles.leftSideBarElementWrapper}>
        <FontAwesomeIcon
          className={`${styles.leftSideBarUserIcon} ${styles.leftSideBarElementAnimation}`}
          icon={AwesomeIcons('user-profile')}
        />
        <span className={styles.leftSideBarUserText}>
          {!isUndefined(userContext.preferredUsername) ? userContext.preferredUsername : userContext.name}
        </span>
      </div>
    </a>
  );
  const renderUserNotifications = () => (
    <a
      href="#"
      onClick={async e => {
        e.preventDefault();
      }}
      title={resources.messages['userSettings']}>
      <div className={styles.leftSideBarElementWrapper}>
        <FontAwesomeIcon
          className={`${styles.leftSideBarUserIcon} ${styles.leftSideBarElementAnimation}`}
          icon={AwesomeIcons('notifications')}
        />
        <span className={styles.leftSideBarUserText}>{resources.messages['notifications']}</span>
      </div>
    </a>
  );
  const renderButtons = () =>
    leftSideBarConfig.buttons.map(button =>
      !button.isLink ? (
        <a href="#">
          <div
            className={styles.leftSideBarElementWrapper}
            onClick={!isUndefined(button.onClick) ? () => button.onClick() : null}>
            <Icon icon={button.icon} className={styles.leftSideBarElementAnimation} />
            <span className={styles.leftSideBarText}>{button.label}</span>
          </div>
        </a>
      ) : (
        <Link to={getUrl(routes[button.linkTo.route], button.linkTo.children, button.linkTo.isRoute)}>
          <div className={styles.leftSideBarElementWrapper}>
            <Icon icon={button.icon} className={styles.leftSideBarElementAnimation} />
            <span className={styles.leftSideBarText}>{button.label}</span>
          </div>
        </Link>
      )
    );
  const renderLogout = () => (
    <a
      href="#userProfilePage"
      title="logout"
      onClick={async e => {
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
      }}>
      <div className={styles.leftSideBarElementWrapper}>
        <Icon icon="logout" className={styles.leftSideBarElementAnimation} />
        <span className={styles.leftSideBarText}>{resources.messages['logout']}</span>
      </div>
    </a>
  );

  return (
    <div
      className={styles.leftSideBar}
      onMouseOver={() => onToggleSideBar(true)}
      onMouseOut={() => onToggleSideBar(false)}>
      {
        <React.Fragment>
          {renderTitle()}
          {/* <hr className={styles.leftSideBarButtonFirstSeparator} /> */}
          {renderUserProfile()}
          {renderUserNotifications()}
          <hr className={styles.leftSideBarButtonSeparator} />
          {!isUndefined(leftSideBarConfig) && leftSideBarConfig.isCustodian ? renderButtons() : null}
          <hr className={styles.leftSideBarButtonLastSeparator} />
          {renderLogout()}
        </React.Fragment>
      }
    </div>
  );
});

export { LeftSideBar };
