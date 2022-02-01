import { useContext } from 'react';
import { useNavigate } from 'react-router-dom';

import logo from 'views/_assets/images/logos/logo.png';
import styles from './Navigation.module.css';

import { routes } from 'conf/routes';

import { UserCard } from './_components/UserCard';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { getUrl } from 'repositories/_utils/UrlUtils';

export const Navigation = () => {
  const navigate = useNavigate();

  const resourcesContext = useContext(ResourcesContext);

  return (
    <div className={styles.header} id="header">
      <a
        className={styles.appLogo}
        href={getUrl(routes.ACCESS_POINT)}
        onClick={e => {
          e.preventDefault();
          navigate(getUrl(routes.ACCESS_POINT));
        }}
        title={resourcesContext.messages['titleHeader']}>
        <img alt="Reportnet" className={styles.appLogo} height="50px" src={logo} />
        <h1 className={styles.appTitle}>{resourcesContext.messages['titleHeader']}</h1>
      </a>
      <UserCard />
    </div>
  );
};
