import React, { Fragment, useEffect, useState } from 'react';

import styles from './EuHeader.module.scss';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { InputText } from 'primereact/inputtext';

export const EuHeader = ({ globanElementStyle, euHeaderElementStyle }) => {
  const [searchInput, setSearchInput] = useState('');
  const [openGloban, setOpenGloban] = useState(false);
  const onSearch = () => {
    window.document.location.href = `https://ec.europa.eu/search/?query_source=TCORNER&QueryText=${encodeURI(
      searchInput
    )}&op=Search&swlang=en&form_build_id=form-8X47OfGhYybVDfQbtJNCrERR9n519jEiL2dVIl2S2Ps&form_id=nexteuropa_europa_search_search_form`;
  };
  return (
    <Fragment>
      <div id="globan" style={globanElementStyle} className={styles.globan}>
        <div className={styles.globanContent}>
          An official website of the European Union
          <a
            href="#globan-dropdown-186d0fazrpn"
            aria-controls="globan-dropdown-186d0fazrpn"
            aria-expanded="false"
            onClick={e => {
              e.preventDefault();
              setOpenGloban(!openGloban);
            }}>
            How do you know?
            {openGloban ? (
              <FontAwesomeIcon className="p-breadcrumb-home" icon={AwesomeIcons('angleSingleUp')} />
            ) : (
              <FontAwesomeIcon className="p-breadcrumb-home" icon={AwesomeIcons('angleDown')} />
            )}
          </a>
          {openGloban && (
            <div class={styles.ban}>
              <p>All official European Union website addresses are in the europa.eu domain.</p>
              <p>
                <a href="//europa.eu/european-union/contact/institutions-bodies_en">
                  See all EU institutions and bodies
                </a>
              </p>
            </div>
          )}
        </div>
      </div>
      <div id="euHeader" style={euHeaderElementStyle} className={styles.euHeader}>
        <div className={styles.europeanUnionLogo}>
          <a href="https://europa.eu/european-union/index_en" title="Home - European Union">
            <span>Home - European Commission</span>
          </a>
        </div>
        <div className={styles.searchBarWrapper}>
          <div className={styles.language}>English</div>
          <div className={styles.searchBar}>
            <InputText onChange={e => setSearchInput(e.target.value)} />
            <button onClick={e => onSearch()}>Search...</button>
          </div>
        </div>
      </div>
    </Fragment>
  );
};
