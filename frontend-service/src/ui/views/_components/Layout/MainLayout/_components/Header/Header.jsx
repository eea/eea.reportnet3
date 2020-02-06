import React, { useContext } from 'react';
import { withRouter } from 'react-router-dom';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import logo from 'assets/images/logo.png';
import styles from './Header.module.scss';

import { routes } from 'ui/routes';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { UserService } from 'core/services/User';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { getUrl } from 'core/infrastructure/CoreUtils';

const Header = withRouter(({ history }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);
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
  const loadUser = () => (
    <>
      <div className={styles.userWrapper}>
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
