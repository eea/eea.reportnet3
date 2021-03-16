import React, { useContext, useEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { config } from 'conf';
import { routes } from 'ui/routes';

import styles from './PublicCountries.module.scss';

import { PublicLayout } from 'ui/views/_components/Layout/PublicLayout';
import ReactCountryFlag from 'react-country-flag';

import { ThemeContext } from 'ui/views/_functions/Contexts/ThemeContext';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { useBreadCrumbs } from 'ui/views/_functions/Hooks/useBreadCrumbs';

import { CurrentPage } from 'ui/views/_functions/Utils';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { EuroFlag } from './components/EuroFlag';

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

  const associatesCountriesCodes = config.countriesByGroup.associates.map(country => country.code);

  const renderCard = isEurope => country => {
    const countryCode = country.code;

    return (
      <div
        href={getUrl(routes.COUNTRY)}
        key={country.code}
        onClick={e => {
          e.preventDefault();
          history.push(getUrl(routes.PUBLIC_COUNTRY_INFORMATION, { countryCode }, true));
        }}
        className={styles.wrapper}>
        <ReactCountryFlag aria-label={country.name} className={styles.flag} countryCode={country.code} svg />

        <div className={styles.titleWrap}>
          <h3>{country.name}</h3>
        </div>

        <div className={styles.euFlagWrapper}>
          {isEurope && !associatesCountriesCodes.includes(countryCode) && <EuroFlag className={styles.euFlag} />}
        </div>
      </div>
    );
  };

  return (
    <PublicLayout>
      <div className={styles.content} style={contentStyles}>
        <div className={`rep-container ${styles.repContainer}`}>
          <h1 className={styles.title}>{resources.messages['countriesPageTitle']}</h1>
          <h2>{resources.messages['eeaCountries']}</h2>
          <div className={styles.countriesWrapper}>{config.countriesByGroup.eeaCountries.map(renderCard(true))}</div>
          <h2>{resources.messages['otherCountries']}</h2>
          <div className={styles.countriesWrapper}>{config.countriesByGroup.otherCountries.map(renderCard(false))}</div>
        </div>
      </div>
    </PublicLayout>
  );
});
