import React, { useContext, useEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { config } from 'conf';
import { routes } from 'ui/routes';

import styles from './PublicCountries.module.scss';

import { PublicLayout } from 'ui/views/_components/Layout/PublicLayout';

import { ThemeContext } from 'ui/views/_functions/Contexts/ThemeContext';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { useBreadCrumbs } from 'ui/views/_functions/Hooks/useBreadCrumbs';

import { CurrentPage } from 'ui/views/_functions/Utils';
import { getUrl } from 'core/infrastructure/CoreUtils';

export const PublicCountries = withRouter(({ history }) => {
  const resources = useContext(ResourcesContext);
  const themeContext = useContext(ThemeContext);

  const [contentStyles, setContentStyles] = useState({});

  useBreadCrumbs({ currentPage: CurrentPage.PUBLIC_COUNTRIES, history });

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
        <div className={`rep-container ${styles.repContainer}`}>
          <h1 className={styles.title}>Countries</h1>
          <div>
            <h3>{resources.messages['eeaCountries']}</h3>
            <div className={styles.countriesWrapper}>
              {config.countriesByGroup.eeaCountries.map(country => {
                const countryCode = country.code;
                return (
                  <div key={country.code}>
                    <a
                      href={getUrl(routes.COUNTRY)}
                      onClick={e => {
                        e.preventDefault();
                        history.push(getUrl(routes.PUBLIC_COUNTRY_INFORMATION, { countryCode }, true));
                      }}>
                      {country.name}
                    </a>
                  </div>
                );
              })}
            </div>
            <h3>{resources.messages['otherCountries']}</h3>
            <div className={styles.countriesWrapper}>
              {config.countriesByGroup.otherCountries.map(country => {
                const countryCode = country.code;
                return (
                  <div key={country.code}>
                    <a
                      href={getUrl(routes.COUNTRY)}
                      onClick={e => {
                        e.preventDefault();
                        history.push(getUrl(routes.PUBLIC_COUNTRY_INFORMATION, { countryCode }, true));
                      }}>
                      {country.name}
                    </a>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      </div>
    </PublicLayout>
  );
});
