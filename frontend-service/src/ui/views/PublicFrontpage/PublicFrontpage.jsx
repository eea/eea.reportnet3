import React from 'react';

import styles from './PublicFrontpage.module.scss';

import { Footer } from 'ui/views/_components/Layout/MainLayout/_components/Footer';
import { Header } from 'ui/views/_components/Layout/MainLayout/_components/Header';
import { EuFooter } from 'ui/views/_components/Layout/MainLayout/_components/EuFooter';
import { TableHeader } from '../_components/DataTable/_components/TableHeader/TableHeader';

export const PublicFrontpage = () => {
  return (
    <div className={styles.mainContainer}>
      <Header />
      <div className={`rep-container`}>
        <div className={`${styles.pageContent} rep-row`}>
          <div className={styles.frontText}>
            <h2>Reportnet 3.0</h2>
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
              http://cdr.eionet.europa.eu/{' '}
            </p>
            <p>
              Instructions on using the platform will be provided to each reporting group through the EEA thematic
              programmes.
            </p>
          </div>
          <div className={styles.currentDataflows}>
            <h3>Dataflows currently (01.08.2020) in scope for Reportnet 3.0:</h3>
          </div>
          <div className={styles.sideBar}></div>
        </div>
      </div>
      <Footer />
      <EuFooter />
    </div>
  );
};
