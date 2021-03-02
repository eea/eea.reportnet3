import React, { useContext, useEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { config } from 'conf';
import { routes } from 'ui/routes';

import styles from './PublicCountries.module.scss';

import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
// import { PublicCard } from 'ui/views/_components/PublicCard';
import { Spinner } from 'ui/views/_components/Spinner';
import { PublicLayout } from 'ui/views/_components/Layout/PublicLayout';

import { ThemeContext } from 'ui/views/_functions/Contexts/ThemeContext';

import { DataflowService } from 'core/services/Dataflow';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { getUrl } from 'core/infrastructure/CoreUtils';

export const PublicCountries = withRouter(({ history }) => {
  const resources = useContext(ResourcesContext);
  const themeContext = useContext(ThemeContext);

  const [contentStyles, setContentStyles] = useState({});
  const [isLoading, setIsLoading] = useState(true);
  const [publicCountries, setPublicCountries] = useState([]);

  useEffect(() => {
    onLoadPublicCountries();
  }, []);

  useEffect(() => {
    if (!themeContext.headerCollapse) {
      setContentStyles({ marginTop: `${config.theme.cookieConsentHeight + 6}px` });
    } else {
      setContentStyles({});
    }
  }, [themeContext.headerCollapse]);

  const onLoadPublicCountries = async () => {
    try {
      const publicData = [
        { id: 1, country: 'SPAIN', countryCode: 'SP' },
        { id: 2, country: 'FRANCE', countryCode: 'FR' }
      ];
      setPublicCountries(publicData);
    } catch (error) {
      console.error('error', error);
    } finally {
      setIsLoading(false);
    }
  };

  const onOpenCountry = countryId => {
    console.log('rowData', countryId);
    return history.push(getUrl(routes.PUBLIC_COUNTRY_INFORMATION, { countryId }, true));
  };

  const renderColumns = countries => {
    const fieldColumns = Object.keys(countries[0])
      .filter(key => key.includes('country') || key.includes('countryCode'))
      .map(field => {
        return <Column field={field} header={resources.messages[field]} key={field} sortable={true} />;
      });

    return fieldColumns;
  };

  return (
    <PublicLayout>
      <div className={styles.content} style={contentStyles}>
        <div className={`rep-container ${styles.repContainer}`}>
          <h1 className={styles.title}>Countries</h1>
          <div className={styles.countriesList}>
            {!isLoading ? (
              <DataTable
                autoLayout={true}
                onRowClick={event => onOpenCountry(event.data.id)}
                totalRecords={publicCountries.length}
                value={publicCountries}>
                {renderColumns(publicCountries)}
              </DataTable>
            ) : (
              <Spinner style={{ top: 0, left: 0 }} />
            )}
          </div>
        </div>
      </div>
    </PublicLayout>
  );
});
