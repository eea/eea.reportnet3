import React, { useState, useContext } from 'react';
import { withRouter, Link } from 'react-router-dom';

import { isUndefined } from 'lodash';

import styles from './LeftSideBar.module.scss';

import { routes } from 'ui/routes';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { Icon } from 'ui/views/_components/Icon';

import { UserService } from 'core/services/User';

import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { getUrl } from 'core/infrastructure/CoreUtils';

const LeftSideBar = withRouter(({ leftSideBarConfig, onToggleSideBar }) => {
  const breadCrumbContext = useContext(BreadCrumbContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

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
      title={resources.messages['notifications']}>
      <div className={styles.leftSideBarElementWrapper}>
        <FontAwesomeIcon
          className={`${styles.leftSideBarUserIcon} ${styles.leftSideBarElementAnimation}`}
          icon={AwesomeIcons('notifications')}
        />
        <span className={styles.notificationCounter}>10</span>
        <span className={styles.leftSideBarUserText}>{resources.messages['notifications']}</span>
      </div>
      {/* <div className={styles.notificationList}><ul><li>Notification 1</li><li>Notification 2</li><li>Notification 2</li><li>Notification 2</li></ul></div> */}
    </a>
  );
  const renderButtons = () =>
    leftSideBarConfig.buttons.map(button =>
      !button.isLink ? (
        <a href="#" title={button.label}>
          <div
            className={styles.leftSideBarElementWrapper}
            onClick={!isUndefined(button.onClick) ? () => button.onClick() : null}>
            <Icon icon={button.icon} className={styles.leftSideBarElementAnimation} title={button.label} />
            <span className={styles.leftSideBarText}>{button.label}</span>
          </div>
        </a>
      ) : (
        <Link
          to={getUrl(routes[button.linkTo.route], button.linkTo.children, button.linkTo.isRoute)}
          title={button.label}>
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
      title={resources.messages['logout']}
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
    <div className={`${styles.leftSideBar}${breadCrumbContext.isLeftSideBarOpened ? ` ${styles.open}` : ''}`}>
      {
        <>
          <div class={styles.barSection}>
            {renderUserProfile()}
            {renderUserNotifications()}
          </div>
          <hr />
          <div class={styles.barSection}>
            {!isUndefined(leftSideBarConfig) && leftSideBarConfig.isCustodian ? renderButtons() : null}
          </div>
          <hr />
          <div class={styles.barSection}>
            {renderLogout()}
            <div className={styles.leftSideBarElementWrapper}>
              <a
                onClick={e => {
                  e.preventDefault();
                  breadCrumbContext.setMenuState();
                }}
                title={
                  breadCrumbContext.isLeftSideBarOpened
                    ? resources.messages['closeSidebar']
                    : resources.messages['expandSidebar']
                }>
                {breadCrumbContext.isLeftSideBarOpened ? (
                  <FontAwesomeIcon icon={AwesomeIcons('angleDoubleLeft')} />
                ) : (
                  <FontAwesomeIcon icon={AwesomeIcons('angleDoubleRight')} />
                )}
              </a>
            </div>
          </div>
        </>
      }
    </div>
  );
});

export { LeftSideBar };
