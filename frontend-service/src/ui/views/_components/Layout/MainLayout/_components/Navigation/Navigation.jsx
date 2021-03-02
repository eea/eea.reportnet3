import React, { useContext } from 'react';
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
    <div id="header" className={styles.header}>
      <a
        href={getUrl(routes.DATAFLOWS)}
        className={styles.appLogo}
        title={resources.messages['titleHeader']}
        onClick={e => {
          e.preventDefault();
          history.push(getUrl(routes.DATAFLOWS));
        }}>
        <img height="50px" src={logo} alt="Reportnet" className={styles.appLogo} />
        <h1 className={styles.appTitle}>{resources.messages['titleHeader']}</h1>
      </a>
      <UserCard />
    </div>
  );
});

export { Navigation };
