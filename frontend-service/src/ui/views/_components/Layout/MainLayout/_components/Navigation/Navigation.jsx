import React, { useContext } from 'react';
import { withRouter } from 'react-router-dom';

import logo from 'assets/images/logo.png';
import styles from './Navigation.module.css';

import { routes } from 'ui/routes';

import { UserCard } from './_components/UserCard';

import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

import { getUrl } from 'core/infrastructure/api/getUrl';

const Navigation = withRouter(({ history }) => {
  const resources = useContext(ResourcesContext);
  return (
    <div id="header" className={styles.header}>
      <a
        href="#home"
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
