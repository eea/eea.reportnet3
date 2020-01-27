import React, { useContext } from 'react';
import { withRouter } from 'react-router-dom';

import logo from 'assets/images/logo.png';
import styles from './Header.module.scss';

import { routes } from 'ui/routes';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { getUrl } from 'core/infrastructure/CoreUtils';

const Header = withRouter(({ history }) => {
  const resources = useContext(ResourcesContext);
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
  return (
    <div id="header" className={styles.header}>
      {loadTitle()}
      <BreadCrumb />
    </div>
  );
});
export { Header };
