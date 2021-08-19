import { useContext, useEffect, useLayoutEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import isNil from 'lodash/isNil';

import { config } from 'conf';
import { routes } from 'conf/routes';

import styles from './PublicFrontpage.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { PublicLayout } from 'views/_components/Layout/PublicLayout';
import Illustration from 'views/_assets/images/logos/public_illustration.png';
import logo from 'views/_assets/images/logos/logo.png';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ThemeContext } from 'views/_functions/Contexts/ThemeContext';

import { useBreadCrumbs } from 'views/_functions/Hooks/useBreadCrumbs';

import { CurrentPage } from 'views/_functions/Utils';
import { getUrl } from 'repositories/_utils/UrlUtils';
import { PublicCard } from '../_components/PublicCard/PublicCard';

import { ErrorUtils } from 'views/_functions/Utils';

export const PublicFrontpage = withRouter(({ history, match }) => {
  const notificationContext = useContext(NotificationContext);
  const themeContext = useContext(ThemeContext);
  const [contentStyles, setContentStyles] = useState({});

  const {
    params: { errorType: urlErrorType }
  } = match;

  useBreadCrumbs({ currentPage: CurrentPage.PUBLIC_INDEX });

  useLayoutEffect(() => {
    if (!themeContext.headerCollapse) {
      setContentStyles({ marginTop: `${config.theme.cookieConsentHeight + 6}px` });
    } else {
      setContentStyles({});
    }
  }, [themeContext.headerCollapse]);

  useEffect(() => {
    if (!isNil(urlErrorType)) {
      notificationContext.add({ type: ErrorUtils.parseErrorType(urlErrorType) });
    }
  }, [urlErrorType]);

  const handleRedirect = target => history.push(target);

  return (
    <PublicLayout>
      <div className={styles.content} style={contentStyles}>
        <div className={styles.mainTextWrapper}>
          <div className={`rep-container`}>
            <div className={`${styles.pageContent} rep-row`}>
              <div className={styles.frontText}>
                <div className={styles.text}>
                  <h2>The next generation for e-Reporting environmental and climate data</h2>
                  <div className={styles.columnsWrapper}>
                    <div className={styles.column}>
                      <p>
                        Reportnet 3 is the new e-Reporting platform for reporting environmental and climate data to the
                        European Environment Agency (EEA). The platform embraces the strategic goals of the European
                        Commission’s Green Deal and Digital Strategy and will host reporting tasks on behalf of EEA and
                        the Commission.
                      </p>
                    </div>
                    <div className={styles.column}>
                      <p>
                        The transition of reporting obligations from Reportnet 2 to Reportnet 3 will take a number of
                        years. Therefore, Reportnet 2 will remain operational until all data flows are migrated and will
                        then become an archive.
                      </p>
                      <p>
                        Reportnet 2 can be accessed here: &nbsp;
                        <a href="http://cdr.eionet.europa.eu/" rel="noopener noreferrer" target="_blank">
                          http://cdr.eionet.europa.eu/
                        </a>
                      </p>
                    </div>
                  </div>
                </div>
                <div className={styles.illustration}>
                  <img alt="Public illustration" rel="preload" src={Illustration} />
                </div>
                <div className={styles.sideBar}>
                  <div className={`${styles.contactBox} ${styles.sideBarItem}`}>
                    <div className={styles.iconWrapper}>
                      <FontAwesomeIcon
                        aria-hidden={false}
                        className={styles.emailIcon}
                        icon={AwesomeIcons('envelope')}
                        role="presentation"
                      />
                    </div>
                    <h4>Need any help?</h4>
                    <p>Please contact us at</p>
                    <p>
                      <a href="mailto:helpdesk@reportnet.europa.eu">helpdesk@reportnet.europa.eu</a>
                    </p>
                  </div>
                  <hr className={styles.separator} />
                  <div className={`${styles.linkBox} ${styles.sideBarItem}`}>
                    <div className={styles.iconWrapper}>
                      <FontAwesomeIcon
                        aria-hidden={false}
                        className={styles.emailIcon}
                        icon={AwesomeIcons('lightPdf')}
                        role="presentation"
                      />
                    </div>
                    <h4>Support documents:</h4>
                    <ul>
                      <li>
                        <a
                          href={`https://www.eionet.europa.eu/reportnet/docs/${window.env.DOCUMENTATION_FOLDER}/howto_login_reportnet3.0`}
                          rel="noopener noreferrer"
                          target="_blank">
                          Login
                        </a>
                      </li>
                      <li>
                        <a
                          href={`https://www.eionet.europa.eu/reportnet/docs/${window.env.DOCUMENTATION_FOLDER}/reporter_howto_reportnet3.0`}
                          rel="noopener noreferrer"
                          target="_blank">
                          Reporter
                        </a>
                      </li>
                      <li>
                        <a
                          href={`https://www.eionet.europa.eu/reportnet/docs/${window.env.DOCUMENTATION_FOLDER}/requester_howto_reportnet3.0`}
                          rel="noopener noreferrer"
                          target="_blank">
                          Requester
                        </a>
                      </li>
                      <li>
                        <a
                          href={`https://www.eionet.europa.eu/reportnet/docs/${window.env.DOCUMENTATION_FOLDER}/webforms_howto_reportnet3.0`}
                          rel="noopener noreferrer"
                          target="_blank">
                          Webforms
                        </a>
                      </li>
                    </ul>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className={`rep-container`}>
          <div className={`${styles.pageContent} rep-row`}>
            <div className={styles.showPublicBox}>
              <div className={styles.title}>
                <h3>Dataflow status</h3>
              </div>
              <div className={styles.showPublicData}>
                <div
                  className={styles.showPublicDataButton}
                  onClick={() => handleRedirect(getUrl(routes.PUBLIC_DATAFLOWS, {}, true))}>
                  <p>
                    <strong>View by obligation dataflow status and download reported data</strong>
                  </p>
                </div>
                <div
                  className={styles.showPublicDataButton}
                  onClick={() => handleRedirect(getUrl(routes.PUBLIC_COUNTRIES, {}, true))}>
                  <p>
                    <strong>View by country dataflow status and download reported data</strong>
                  </p>
                </div>
              </div>
            </div>
            <div className={styles.currentDataflows}>
              <h3>Dataflows in scope of Reportnet 3:</h3>
              <div className={styles.dataflowsList}>
                {config.publicFrontpage.dataflows.map(dataflow => (
                  <PublicCard
                    card={dataflow}
                    dataflowId={dataflow.id}
                    dueDate={dataflow.targetDate}
                    externalCard={true}
                    frequency={dataflow.reportingFrequency}
                    key={dataflow.key}
                    pilotScenarioAmbition={dataflow.pilotScenarioAmbition}
                    subtitle={{ text: dataflow.legalInstrument, url: dataflow.legalInstrumentUrl }}
                    title={{ text: dataflow.dataflow, url: dataflow.dataFlowUrl }}
                  />
                ))}
              </div>
              <h3>EEA Voluntary Dataflows:</h3>
              <div className={styles.dataflowsList}>
                {config.publicFrontpage.voluntaryDataflows.map(dataflow => (
                  <PublicCard
                    card={dataflow}
                    dataflowId={dataflow.id}
                    dueDate={dataflow.targetDate}
                    externalCard={true}
                    frequency={dataflow.reportingFrequency}
                    key={dataflow.key}
                    pilotScenarioAmbition={dataflow.pilotScenarioAmbition}
                    subtitle={{ text: dataflow.legalInstrument, url: dataflow.legalInstrumentUrl }}
                    title={{ text: dataflow.dataflow, url: dataflow.dataFlowUrl }}
                  />
                ))}
              </div>
            </div>
            <div className={styles.otherPortals}>
              <div className={styles.title}>
                <h3>External portals</h3>
              </div>
              <div className={styles.portalList}>
                <a
                  className={styles.portalBox}
                  href="http://cdr.eionet.europa.eu/"
                  rel="noopener noreferrer"
                  target="_blank">
                  <img alt="Reportnet 2 Portal" height="50px" src={logo} />
                  <h4>Reportnet 2</h4>
                  <p>Reportnet is Eionet’s infrastructure for supporting and improving data and information flows.</p>
                </a>
                <a
                  className={styles.portalBox}
                  href="https://rod.eionet.europa.eu/"
                  rel="noopener noreferrer"
                  target="_blank">
                  <img alt="ROD 3 Portal" height="50px" src={logo} />
                  <h4>ROD 3</h4>
                  <p>EEA's reporting obligations database</p>
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </PublicLayout>
  );
});
