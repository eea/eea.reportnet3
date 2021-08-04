import { useContext } from 'react';
import { withRouter } from 'react-router-dom';

import styles from './Footer.module.scss';

import { routes } from 'conf/routes';

import eeaLogo from 'views/_assets/images/logos/reportnet-3.0-logo.png';
import eeaLogoDark from 'views/_assets/images/logos/reportnet-3.0-logo-white.png';
import logo from 'views/_assets/images/logos/logo.png';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { ThemeContext } from 'views/_functions/Contexts/ThemeContext';

import { getUrl } from 'repositories/_utils/UrlUtils';

export const Footer = withRouter(({ history }) => {
  const resources = useContext(ResourcesContext);
  const themeContext = useContext(ThemeContext);

  return (
    <div className={styles.Footer}>
      <div className={styles.footerContent}>
        <div className={styles.reportnetLogo}>
          <a className={styles.title} href="https://www.eea.europa.eu/" title={resources.messages['eea']}>
            <img
              alt={resources.messages['eea']}
              className={styles.appLogo}
              height="50px"
              src={themeContext.currentTheme !== 'dark' ? eeaLogo : eeaLogoDark}
            />
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
