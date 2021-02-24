import React, { useContext, useEffect, useState } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { config } from 'conf';

import styles from './PublicFrontpage.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Card } from './_components/Card';
import { PublicLayout } from 'ui/views/_components/Layout/PublicLayout';
import Illustration from 'assets/images/public_illustration.png';
import logo from 'assets/images/logo.png';

import { ThemeContext } from 'ui/views/_functions/Contexts/ThemeContext';

export const PublicFrontpage = () => {
  const themeContext = useContext(ThemeContext);
  const [contentStyles, setContentStyles] = useState({});

  useEffect(() => {
    if (!themeContext.headerCollapse) {
      setContentStyles({ marginTop: `${config.theme.cookieConsentHeight + 6}px` });
    } else {
      setContentStyles({});
    }
  }, [themeContext.headerCollapse]);

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
                        Reportnet 3.0 is the new e-Reporting platform for reporting environmental and climate data to
                        the European Environment Agency (EEA). The platform embraces the strategic goals of the European
                        Commission’s Green Deal and Digital Strategy and will host reporting tasks on behalf of EEA and
                        the Commission.
                      </p>
                    </div>
                    <div className={styles.column}>
                      <p>
                        The transition of reporting obligations from Reportnet 2.0 to Reportnet 3.0 will take a number
                        of years. Therefore, Reportnet 2.0 will remain operational until all data flows are migrated and
                        will then become an archive.
                      </p>
                      <p>
                        Reportnet 2.0 can be accessed here: &nbsp;
                        <a href="http://cdr.eionet.europa.eu/" target="_blank" rel="noopener noreferrer">
                          http://cdr.eionet.europa.eu/
                        </a>
                      </p>
                    </div>
                  </div>
                </div>
                <div className={styles.illustration}>
                  <img src={Illustration} />
                </div>
                <div className={styles.sideBar}>
                  <div className={`${styles.contactBox} ${styles.sideBarItem}`}>
                    <div className={styles.iconWrapper}>
                      <FontAwesomeIcon
                        aria-hidden={false}
                        className={styles.emailIcon}
                        icon={AwesomeIcons('envelope')}
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
                      />
                    </div>
                    <h4>Support documents:</h4>
                    <ul>
                      <li>
                        <a
                          href={`https://www.eionet.europa.eu/reportnet/docs/${window.env.DOCUMENTATION_FOLDER}/howto_login_reportnet3.0`}
                          target="_blank"
                          rel="noopener noreferrer">
                          Login
                        </a>
                      </li>
                      <li>
                        <a
                          href={`https://www.eionet.europa.eu/reportnet/docs/${window.env.DOCUMENTATION_FOLDER}/reporter_howto_reportnet3.0`}
                          target="_blank"
                          rel="noopener noreferrer">
                          Reporter
                        </a>
                      </li>
                      <li>
                        <a
                          href={`https://www.eionet.europa.eu/reportnet/docs/${window.env.DOCUMENTATION_FOLDER}/requester_howto_reportnet3.0`}
                          target="_blank"
                          rel="noopener noreferrer">
                          Requester
                        </a>
                      </li>
                      <li>
                        <a
                          href={`https://www.eionet.europa.eu/reportnet/docs/${window.env.DOCUMENTATION_FOLDER}/webforms_howto_reportnet3.0`}
                          target="_blank"
                          rel="noopener noreferrer">
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
            <div className={styles.currentDataflows}>
              <h3>Dataflows in scope of Reportnet 3.0:</h3>
              <div className={styles.dataflowsList}>
                {config.publicFrontpage.dataflows.map(dataflow => (
                  <Card {...dataflow} />
                ))}
              </div>
              <h3>EEA Voluntary Dataflows:</h3>
              <div className={styles.dataflowsList}>
                {config.publicFrontpage.voluntaryDataflows.map(dataflow => (
                  <Card {...dataflow} />
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
                  target="_blank"
                  rel="noopener noreferrer">
                  <img height="50px" src={logo} alt="Reportnet 2.0 Portal" />
                  <h4>Reportnet 2</h4>
                  <p>Reportnet is Eionet’s infrastructure for supporting and improving data and information flows.</p>
                </a>
                <a
                  className={styles.portalBox}
                  href="https://rod.eionet.europa.eu/"
                  target="_blank"
                  rel="noopener noreferrer">
                  <img height="50px" src={logo} alt="ROD 3 Portal" />
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
};
