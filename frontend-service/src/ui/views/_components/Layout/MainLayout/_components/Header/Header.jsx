import React, { useContext, useEffect, useRef, useState } from 'react';
import { withRouter } from 'react-router-dom';
import isEmpty from 'lodash/isEmpty';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import defaultAvatar from 'assets/images/avatars/defaultAvatar.png';
import logo from 'assets/images/logo.png';
import styles from './Header.module.scss';

import { routes } from 'ui/routes';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { UserService } from 'core/services/User';
import { InputSwitch } from 'ui/views/_components/InputSwitch';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { ThemeContext } from 'ui/views/_functions/Contexts/ThemeContext';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { getUrl } from 'core/infrastructure/CoreUtils';

const Header = withRouter(({ history }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);
  const themeContext = useContext(ThemeContext);

  const avatarImage = useRef();

  const [confirmvisible, setConfirmVisible] = useState(false);

  useEffect(() => {
    if (!isEmpty(userContext.userProps.userImage) && userContext.userProps.userImage.join('') !== '') {
      onLoadImage();
    }
  }, [userContext.userProps.userImage]);

  const loadTitle = () => (
    <a
      href={getUrl(routes.DATAFLOWS)}
      className={styles.title}
      title={resources.messages['titleHeader']}
      onClick={e => {
        e.preventDefault();
        history.push(getUrl(routes.DATAFLOWS));
      }}>
      <img height="50px" src={logo} alt="Reportnet" className={styles.appLogo} />
      <h1 className={styles.appTitle}>{resources.messages['titleHeader']}</h1>
    </a>
  );

  const isLocalEnvironment = () => {
    let url = window.location.href;
    if (url.toString().includes('localhost')) {
      return true;
    }
    return false;
  };

  const localhostEnvironmentAlert = isLocalEnvironment() && (
    <div className={styles.localhostAlert}>
      <FontAwesomeIcon icon={AwesomeIcons('localhostAlert')} title={resources.messages['localhostAlert']} />
    </div>
  );

  const themeSwitcher = isLocalEnvironment() && (
    <InputSwitch
      checked={themeContext.currentTheme === 'dark'}
      onChange={e => {
        userContext.onToggleVisualTheme(e.value ? 'dark' : 'light');
        themeContext.onToggleTheme(e.value ? 'dark' : 'light');
      }}
      sliderCheckedClassName={styles.themeSwitcherInputSwitch}
      style={{ marginRight: '1rem' }}
      tooltip={
        themeContext.currentTheme === 'light'
          ? resources.messages['toggleDarkTheme']
          : resources.messages['toggleLightTheme']
      }
      tooltipOptions={{ position: 'bottom', className: styles.themeSwitcherTooltip }}
    />
  );

  const userLogout = async () => {
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
  };

  const userProfileSettingsButton = (
    <a
      className="userSettingsBtn"
      href={getUrl(routes.SETTINGS)}
      title="User profile details"
      onClick={async e => {
        e.preventDefault();
        history.push(getUrl(routes.SETTINGS));
      }}>
      <img
        ref={avatarImage}
        icon={<FontAwesomeIcon icon={AwesomeIcons('user-profile')} className={styles.userDataIcon} />}
        src={isEmpty(userContext.userProps.userImage) ? defaultAvatar : null}
        className={styles.userAvatar}
      />
      {/* <FontAwesomeIcon className={styles.avatar} icon={AwesomeIcons('user-profile')} />{' '} */}
      <span>{userContext.preferredUsername}</span>
    </a>
  );

  const logout = (
    <div className={styles.logoutWrapper}>
      <FontAwesomeIcon
        className={styles.logoutButton}
        onClick={async e => {
          e.preventDefault();
          userContext.userProps.showLogoutConfirmation ? setConfirmVisible(true) : userLogout();
        }}
        icon={AwesomeIcons('logout')}
      />
    </div>
  );

  const loadUser = () => (
    <>
      <div className={styles.userWrapper}>
        {themeSwitcher}
        {localhostEnvironmentAlert}
        {userProfileSettingsButton}
      </div>

      <div className={styles.logoutWrapper}>{logout}</div>
    </>
  );

  const onLoadImage = () => {
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    const { current } = avatarImage;
    current.onload = function () {
      ctx.drawImage(current, 0, 0);
    };
    current.src = userContext.userProps.userImage.join('');
  };

  return (
    <div id="header" className={styles.header}>
      {loadTitle()}
      <BreadCrumb />
      {loadUser()}
      {userContext.userProps.showLogoutConfirmation && (
        <ConfirmDialog
          onConfirm={() => {
            userLogout();
          }}
          onHide={() => setConfirmVisible(false)}
          visible={confirmvisible}
          header={resources.messages['logout']}
          labelConfirm={resources.messages['yes']}
          labelCancel={resources.messages['no']}>
          {resources.messages['userLogout']}
        </ConfirmDialog>
      )}
    </div>
  );
});
export { Header };
