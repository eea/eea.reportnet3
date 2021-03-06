import { useContext, useEffect, useState } from 'react';
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
import { EuroFlag } from './_components/EuroFlag';

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

  const renderCountryCard = isEurope => country => {
    const countryCode = country.code;

    const isAssociate = associatesCountriesCodes.includes(countryCode);

    return (
      <div
        className={styles.wrapper}
        href={getUrl(routes.COUNTRY)}
        key={country.code}
        onClick={e => {
          e.preventDefault();
          history.push(getUrl(routes.PUBLIC_COUNTRY_INFORMATION, { countryCode }, true));
        }}>
        <ReactCountryFlag aria-label={country.name} className={styles.flag} countryCode={country.code} svg />

        <div className={styles.titleWrap}>
          <h3>{country.name}</h3>
        </div>

        <div className={isEurope ? styles.euFlagWrapper : ''}>
          {isEurope && !isAssociate ? <EuroFlag className={styles.euFlag} /> : ''}
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
          <div className={styles.countriesWrapper}>
            {config.countriesByGroup.eeaCountries.map(renderCountryCard(true))}
          </div>

          <h2>{resources.messages['cooperatingCountries']}</h2>
          <div className={styles.countriesWrapper}>
            {config.countriesByGroup.cooperatingCountries.map(renderCountryCard(false))}
          </div>

          <h2>{resources.messages['otherCountries']}</h2>
          <div className={styles.countriesWrapper}>
            {config.countriesByGroup.otherCountries.map(renderCountryCard(false))}
          </div>
        </div>
      </div>
    </PublicLayout>
  );
});
