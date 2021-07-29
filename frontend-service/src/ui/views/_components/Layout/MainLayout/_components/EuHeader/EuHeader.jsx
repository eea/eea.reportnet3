import { Fragment, useContext, useState } from 'react';

import styles from './EuHeader.module.scss';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { CookiesDialog } from 'ui/views/_components/CookiesDialog';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

export const EuHeader = ({ euHeaderElementStyle, globanElementStyle }) => {
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [openGloban, setOpenGloban] = useState(false);

  return (
    <Fragment>
      <div className={styles.globan} id="globan" style={globanElementStyle}>
        <div className={styles.globanContent}>
          <span>{resources.messages['anOfficialWebsite']}</span>
          <span
            onClick={e => {
              e.preventDefault();
              setOpenGloban(!openGloban);
            }}>
            {resources.messages['howDoYouKnow']}
            {openGloban ? (
              <FontAwesomeIcon
                ariaLabel={resources.messages['howDoYouKnow']}
                className="p-breadcrumb-home"
                icon={AwesomeIcons('angleSingleUp')}
                role="button"
              />
            ) : (
              <FontAwesomeIcon
                aria-label={resources.messages['howDoYouKnow']}
                className="p-breadcrumb-home"
                icon={AwesomeIcons('angleDown')}
                role="button"
              />
            )}
          </span>
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
      <div className={styles.euHeader} id="euHeader" style={euHeaderElementStyle}>
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
        <div className={styles.searchBarWrapper}></div>
      </div>
    </Fragment>
  );
};
