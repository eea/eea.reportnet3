import { useContext } from 'react';
import { withRouter } from 'react-router-dom';

import logo from 'assets/images/logos/logo.png';
import styles from './Navigation.module.css';

import { routes } from 'ui/routes';

import { UserCard } from './_components/UserCard';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { getUrl } from 'core/infrastructure/CoreUtils';

const Navigation = withRouter(({ history }) => {
  const resources = useContext(ResourcesContext);
  return (
    <div className={styles.header} id="header">
      <a
        className={styles.appLogo}
        href={getUrl(routes.ACCESS_POINT)}
        onClick={e => {
          e.preventDefault();
          history.push(getUrl(routes.ACCESS_POINT));
        }}
        title={resources.messages['titleHeader']}>
        <img alt="Reportnet" className={styles.appLogo} height="50px" src={logo} />
        <h1 className={styles.appTitle}>{resources.messages['titleHeader']}</h1>
      </a>
      <UserCard />
    </div>
  );
});

export { Navigation };
