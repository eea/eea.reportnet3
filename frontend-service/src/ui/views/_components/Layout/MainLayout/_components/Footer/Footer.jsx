import React, { useContext, Fragment } from 'react';
import { withRouter } from 'react-router-dom';

import styles from './Footer.module.scss';

import { routes } from 'ui/routes';

import logo from 'assets/images/logo.png';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { getUrl } from 'core/infrastructure/CoreUtils';

export const Footer = withRouter(({ history, leftMargin }) => {
  const resources = useContext(ResourcesContext);
  return (
    <Fragment>
      <div className={styles.Footer} style={{ marginLeft: leftMargin, transition: '0.5s' }}>
        <div className="rep-container">
          <div className={styles.footerContent}>
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
              <a href="mailto:reportnet.helpdesk@eea.europa.eu">reportnet.helpdesk@eea.europa.eu</a>
            </div>
          </div>
        </div>
      </div>
    </Fragment>
  );
});
