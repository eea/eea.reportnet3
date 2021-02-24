import React, { Fragment, useContext, useState } from 'react';

import styles from './EuHeader.module.scss';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { CookiesDialog } from 'ui/views/_components/CookiesDialog';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

export const EuHeader = ({ euHeaderElementStyle, globanElementStyle }) => {
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  // const [searchInput, setSearchInput] = useState('');
  const [openGloban, setOpenGloban] = useState(false);

  // const onSearch = () => {
  //   window.document.location.href = `https://ec.europa.eu/search/?query_source=TCORNER&QueryText=${encodeURI(
  //     searchInput
  //   )}&op=Search&swlang=en&form_build_id=form-8X47OfGhYybVDfQbtJNCrERR9n519jEiL2dVIl2S2Ps&form_id=nexteuropa_europa_search_search_form`;
  // };

  return (
    <Fragment>
      <div id="globan" style={globanElementStyle} className={styles.globan}>
        <div className={styles.globanContent}>
          <span>{resources.messages['anOfficialWebsite']}</span>
          <a
            href="#globan-dropdown-186d0fazrpn"
            aria-controls="globan-dropdown-186d0fazrpn"
            aria-expanded="false"
            onClick={e => {
              e.preventDefault();
              setOpenGloban(!openGloban);
            }}>
            {resources.messages['howDoYouKnow']}
            {openGloban ? (
              <FontAwesomeIcon className="p-breadcrumb-home" icon={AwesomeIcons('angleSingleUp')} />
            ) : (
              <FontAwesomeIcon className="p-breadcrumb-home" icon={AwesomeIcons('angleDown')} />
            )}
          </a>
          {openGloban && (
            <div class={styles.ban}>
              <p>{resources.messages['allOfficialEuropeanUnionWebsiteAddresses']}</p>
              <p>
                <a href="//europa.eu/european-union/contact/institutions-bodies_en">
                  {resources.messages['seeAllEUInstitutions']}
                </a>
              </p>
            </div>
          )}
        </div>
      </div>
      <CookiesDialog />
      <div id="euHeader" style={euHeaderElementStyle} className={styles.euHeader}>
        <div className={styles.europeanUnionLogo}>
          <a
            className={`${
              userContext.userProps.visualTheme === 'light'
                ? styles.europeanUnionBlackLogo
                : styles.europeanUnionWhiteLogo
            }`}
            href="https://europa.eu/european-union/index_en"
            title="Home - European Union">
            <span>{resources.messages['home']}</span>
          </a>
        </div>
        <div className={styles.searchBarWrapper}>
          {/* <div className={styles.language}>English</div> */}
          {/* <div className={styles.searchBar}>
            <InputText onChange={e => setSearchInput(e.target.value)} />
            <button onClick={e => onSearch()}>{resources.messages['search...']}</button>
          </div> */}
        </div>
      </div>
    </Fragment>
  );
};
