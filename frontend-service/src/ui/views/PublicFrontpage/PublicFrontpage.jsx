import React, { useState, useContext } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { config } from 'conf';

import styles from './PublicFrontpage.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Card } from './_components/Card';
import { EuFooter } from 'ui/views/_components/Layout/MainLayout/_components/EuFooter';
import { Footer } from 'ui/views/_components/Layout/MainLayout/_components/Footer';
import { Header } from 'ui/views/_components/Layout/MainLayout/_components/Header';
import logo from 'assets/images/logo.png';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const PublicFrontpage = () => {
  const resourcesContext = useContext(ResourcesContext);

  return (
    <div className={styles.mainContainer}>
      <Header isPublic={true} />
      <div className={`rep-container`}>
        <div className={`${styles.pageContent} rep-row`}>
          <div className={styles.frontText}>
            <h2>{resourcesContext.messages['appName']}</h2>
            <p>
              Operational in July 2020, Reportnet 3.0 is the next generation of platform for reporting environmental
              data to the EEA, and will also host several reporting tasks of DG for Environment and DG for Climate
              Action. Reportnet 3.0 is a centralized e-Reporting platform, aiming at simplifying and streamlining the
              data flow steps across all environmental domains. The system acts as a one-stop-shop for all involved
              stakeholders. It effectively addresses the issues previously faced by the reporters and employ modern
              approaches in software development (i.e. with regards to security, scalability, architecture,
              interoperability, etc.).
            </p>
            <p>
              The vision of Reportnet 3.0 is designed to deliver the ambition and the strategic goals as set out by the
              European Commission's Digital Strategy. It is the EEA's contribution to deliver on this long-term
              strategy.
            </p>
            <p>
              The transition of reporting obligations from Reportnet 2.0 to Reportnet 3.0 will take a number of years.
              Therefore, Reportnet 2.0 will remain partly operational until 2025 when the last obligations will be
              transitioned, and then will become an archive. Reportnet 2.0 can be accessed from here:
              <a href="http://cdr.eionet.europa.eu/">http://cdr.eionet.europa.eu/</a>
            </p>
            <p>
              Instructions on using the platform will be provided to each reporting group through the EEA thematic
              programmes.
            </p>
          </div>
          <div className={styles.currentDataflows}>
            <h3>Dataflows currently (01.08.2020) in scope for Reportnet 3.0:</h3>
            <div className={styles.dataflowsList}>
              {config.publicFrontpage.dataflows.map(dataflow => (
                <Card {...dataflow} />
              ))}
            </div>
          </div>
          <div className={styles.otherPortals}>
            <div className={styles.title}>
              <h3>Popular external portals</h3>
            </div>
            <div className={styles.portalList}>
              <div
                className={styles.portalBox}
                onClick={e => {
                  window.location.href = 'http://cdr.eionet.europa.eu/';
                }}>
                <img height="50px" src={logo} alt="Reportnet 2.0 Portal" />
                <h4>Reportnet 2</h4>
                <p>Reportnet is Eionet’s infrastructure for supporting and improving data and information flows.</p>
              </div>
              <div
                className={styles.portalBox}
                onClick={e => {
                  window.location.href = 'https://rod.eionet.europa.eu/';
                }}>
                <img height="50px" src={logo} alt="ROD 3 Portal" />
                <h4>ROD 3</h4>
                <p>EEA's reporting obligations database</p>
              </div>
              <div className={styles.contactBox}>
                <div className={styles.iconWrapper}>
                  <FontAwesomeIcon aria-hidden={false} className={styles.emailIcon} icon={AwesomeIcons('envelope')} />
                </div>
                <h4>Need any help?</h4>
                <p>Please contact us at</p>
                <p>
                  <a href="mailto:helpdesk@eionet.europa.eu">helpdesk@eionet.europa.eu</a>
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
      <Footer />
      <EuFooter />
    </div>
  );
};
