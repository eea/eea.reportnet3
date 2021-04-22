import { Fragment, useContext } from 'react';
import { withRouter } from 'react-router-dom';

import styles from './Footer.module.scss';

import { routes } from 'ui/routes';

import eeaLogo from 'assets/images/logos/reportnet-3.0-logo.png';
import logo from 'assets/images/logos/logo.png';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { getUrl } from 'core/infrastructure/CoreUtils';

export const Footer = withRouter(({ history, leftMargin }) => {
  const resources = useContext(ResourcesContext);

  return (
    <Fragment>
      <div className={styles.Footer}>
        <div className={styles.footerContent}>
          <div className={styles.reportnetLogo}>
            <a href="https://www.eea.europa.eu/" className={styles.title} title={resources.messages['eea']}>
              <img height="50px" src={eeaLogo} alt={resources.messages['eea']} className={styles.appLogo} />
            </a>
          </div>
          <div className={styles.reportnetLogo}>
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
          </div>
          <div className={styles.helpDesk}>
            <a href="mailto:helpdesk@reportnet.europa.eu">helpdesk@reportnet.europa.eu</a>
          </div>
        </div>
      </div>
    </Fragment>
  );
});
