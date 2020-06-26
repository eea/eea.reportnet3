import React, { Fragment } from 'react';

import styles from './EuHeader.module.scss';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { InputText } from 'primereact/inputtext';

export const EuHeader = () => {
  return (
    <Fragment>
      <div id="globan" className={styles.globan}>
        <div className={styles.globanContent}>
          An official website of the European Union
          <a href="#globan-dropdown-186d0fazrpn" aria-controls="globan-dropdown-186d0fazrpn" aria-expanded="false">
            How do you know?
            <FontAwesomeIcon className="p-breadcrumb-home" icon={AwesomeIcons('angleDown')} />
          </a>
        </div>
      </div>
      <div className={styles.euHeader}>
        <div className={styles.europeanUnionLogo}>
          <a href="https://europa.eu/european-union/index_en" title="Home - European Union">
            <span>Home - European Commission</span>
          </a>
        </div>
        <div className={styles.searchBarWrapper}>
          <div className={styles.language}>English</div>
          <div className={styles.searchBar}>
            <InputText />
            <button>Search...</button>
          </div>
        </div>
      </div>
    </Fragment>
  );
};
