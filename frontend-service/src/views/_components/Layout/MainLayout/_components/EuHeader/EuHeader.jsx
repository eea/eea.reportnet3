import { Fragment, useContext, useState } from 'react';

import styles from './EuHeader.module.scss';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { CookiesDialog } from 'views/_components/CookiesDialog';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

export const EuHeader = ({ euHeaderElementStyle, globanElementStyle }) => {
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [openGloban, setOpenGloban] = useState(false);

  return (
    <Fragment>
      <div className={styles.globan} id="globan" style={globanElementStyle}>
        <div className={styles.globanContent}>
          <span>{resourcesContext.messages['anOfficialWebsite']}</span>
          <span
            onClick={e => {
              e.preventDefault();
              setOpenGloban(!openGloban);
            }}>
            {resourcesContext.messages['howDoYouKnow']}
            {openGloban ? (
              <FontAwesomeIcon
                ariaLabel={resourcesContext.messages['howDoYouKnow']}
                className="p-breadcrumb-home"
                icon={AwesomeIcons('angleSingleUp')}
                role="button"
              />
            ) : (
              <FontAwesomeIcon
                ariaLabel={resourcesContext.messages['howDoYouKnow']}
                className="p-breadcrumb-home"
                icon={AwesomeIcons('angleDown')}
                role="button"
              />
            )}
          </span>
          {openGloban && (
            <div class={styles.ban}>
              <p>{resourcesContext.messages['allOfficialEuropeanUnionWebsiteAddresses']}</p>
              <p>
                <a href="//europa.eu/european-union/contact/institutions-bodies_en">
                  {resourcesContext.messages['seeAllEUInstitutions']}
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
            <span>{resourcesContext.messages['home']}</span>
          </a>
        </div>
        <div className={styles.searchBarWrapper}></div>
      </div>
    </Fragment>
  );
};
