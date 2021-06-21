import { useContext } from 'react';
import { withRouter } from 'react-router-dom';

import styles from './Footer.module.scss';

import { routes } from 'ui/routes';

import eeaLogo from 'assets/images/logos/reportnet-3.0-logo.png';
import logo from 'assets/images/logos/logo.png';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { getUrl } from 'core/infrastructure/CoreUtils';

export const Footer = withRouter(({ history }) => {
  const resources = useContext(ResourcesContext);

  return (
    <div className={styles.Footer}>
      <div className={styles.footerContent}>
        <div className={styles.reportnetLogo}>
          <a className={styles.title} href="https://www.eea.europa.eu/" title={resources.messages['eea']}>
            <img alt={resources.messages['eea']} className={styles.appLogo} height="50px" src={eeaLogo} />
          </a>
        </div>
        <div className={styles.reportnetLogo}>
          <a
            className={styles.title}
            href={getUrl(routes.ACCESS_POINT)}
            onClick={e => {
              e.preventDefault();
              history.push(getUrl(routes.ACCESS_POINT));
            }}
            title={resources.messages['titleHeader']}>
            <img alt="Reportnet app logo" className={styles.appLogo} height="50px" src={logo} />
            <h1 className={styles.appTitle}>{resources.messages['titleHeader']}</h1>
          </a>
        </div>
        <div className={styles.helpDesk}>
          <a href="mailto:helpdesk@reportnet.europa.eu">helpdesk@reportnet.europa.eu</a>
        </div>
      </div>
    </div>
  );
});
