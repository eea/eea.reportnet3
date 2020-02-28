import React, { useContext } from 'react';
import { withRouter } from 'react-router-dom';

import { AwesomeIcons } from 'conf/AwesomeIcons';

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

import { getUrl } from 'core/infrastructure/CoreUtils';

const Header = withRouter(({ history }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);
  const themeContext = useContext(ThemeContext);

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
  const localhostEnvironmentAlert = () => {
    if (!isLocalEnvironment()) {
      return;
    } else
      return (
        <div className={styles.localhostAlert}>
          <FontAwesomeIcon icon={AwesomeIcons('localhostAlert')} title={resources.messages['localhostAlert']} />
        </div>
      );
  };
  const loadUser = () => (
    <>
      <div className={styles.userWrapper}>
        <InputSwitch
          checked={themeContext.currentTheme === 'dark'}
          onChange={e => themeContext.onToggleTheme(e.value ? 'dark' : 'light')}
          sliderCheckedClassName={styles.themeSwitcherInputSwitch}
          style={{ marginRight: '1rem' }}
          tooltip={
            themeContext.currentTheme === 'light'
              ? resources.messages['toggleDarkTheme']
              : resources.messages['toggleLightTheme']
          }
          tooltipOptions={{ position: 'bottom', className: styles.themeSwitcherTooltip }}
        />
        {localhostEnvironmentAlert()}
        <FontAwesomeIcon icon={AwesomeIcons('user-profile')} /> <span>{userContext.preferredUsername}</span>
      </div>
      <div className={styles.logoutWrapper}>
        <FontAwesomeIcon
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
          }}
          icon={AwesomeIcons('logout')}
        />
      </div>
    </>
  );
  return (
    <div id="header" className={styles.header}>
      {loadTitle()}
      <BreadCrumb />
      {loadUser()}
    </div>
  );
});
export { Header };
