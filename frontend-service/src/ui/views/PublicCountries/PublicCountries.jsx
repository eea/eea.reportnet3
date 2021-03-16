import React, { useContext, useEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';

import europeanFlag from 'assets/images/logos/europeanFlag.png';
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

  const renderCountryCard = countries => {
    const countryCard = countries.map(country => {
      const countryCode = country.code;
      return (
        <div
          href={getUrl(routes.COUNTRY)}
          key={country.code}
          onClick={e => {
            e.preventDefault();
            history.push(getUrl(routes.PUBLIC_COUNTRY_INFORMATION, { countryCode }, true));
          }}>
          <img alt={country.name} className={styles.country} src={europeanFlag}></img>
          <h3>{country.name}</h3>
          <ReactCountryFlag aria-label={country.name} className={styles.flag} countryCode={country.code} svg />
        </div>
      );
    });
    return countryCard;
  };

  return (
    <PublicLayout>
      <div className={styles.content} style={contentStyles}>
        <div className={`rep-container ${styles.repContainer}`}>
          <h1 className={styles.title}>{resources.messages['countriesPageTitle']}</h1>
          <h2>{resources.messages['eeaCountries']}</h2>
          <div className={styles.countriesWrapper}>{renderCountryCard(config.countriesByGroup.eeaCountries)}</div>
          <h2>{resources.messages['cooperatingCountries']}</h2>
          <div className={styles.countriesWrapper}>
            {renderCountryCard(config.countriesByGroup.cooperatingCountries)}
          </div>
          <h2>{resources.messages['otherCountries']}</h2>
          <div className={styles.countriesWrapper}>{renderCountryCard(config.countriesByGroup.otherCountries)}</div>
        </div>
      </div>
    </PublicLayout>
  );
});
