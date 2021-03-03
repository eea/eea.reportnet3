import React, { useContext, useEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';
import ReactTooltip from 'react-tooltip';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { config } from 'conf';
import { routes } from 'ui/routes';

import styles from './PublicCountryInformation.module.scss';

import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
// import { PublicCard } from 'ui/views/_components/PublicCard';
import { Spinner } from 'ui/views/_components/Spinner';
import { PublicLayout } from 'ui/views/_components/Layout/PublicLayout';

import { ThemeContext } from 'ui/views/_functions/Contexts/ThemeContext';

import { DataflowService } from 'core/services/Dataflow';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { useBreadCrumbs } from 'ui/views/_functions/Hooks/useBreadCrumbs';

import { CurrentPage } from 'ui/views/_functions/Utils';

export const PublicCountryInformation = withRouter(({ match, history }) => {
  const {
    params: { countryId }
  } = match;

  const resources = useContext(ResourcesContext);
  const themeContext = useContext(ThemeContext);

  const [contentStyles, setContentStyles] = useState({});
  const [isLoading, setIsLoading] = useState(true);
  const [publicCountryInformation, setPublicCountryInformation] = useState([]);

  useBreadCrumbs({ currentPage: CurrentPage.PUBLIC_COUNTRY, countryId, history });

  useEffect(() => {
    onLoadPublicCountryInformation();
  }, []);

  useEffect(() => {
    if (!themeContext.headerCollapse) {
      setContentStyles({ marginTop: `${config.theme.cookieConsentHeight + 6}px` });
    } else {
      setContentStyles({});
    }
  }, [themeContext.headerCollapse]);

  const getPublicFileName = fileName => {
    const splittedFileName = fileName.split('-');
    return splittedFileName[1];
  };

  const downloadFileBodyColumn = rowData => {
    if (rowData.publicsFileName != 0) {
      return (
        <div className={styles.filesContainer}>
          {rowData.publicsFileName.map(publicFileName => (
            <span className={styles.downloadIcon}>
              {/* onClick={() => onFileDownload(rowData.dataProviderId, publicFileName)}> */}
              <FontAwesomeIcon icon={AwesomeIcons('xlsx')} data-tip data-for={publicFileName} />
              <ReactTooltip className={styles.tooltipClass} effect="solid" id={publicFileName} place="top">
                <span>{getPublicFileName(publicFileName)}</span>
              </ReactTooltip>
            </span>
          ))}
        </div>
      );
    }
  };

  const isReleasedBodyColumn = rowData => {
    return (
      <div className={styles.checkedValueColumn}>
        {rowData.isReleased ? <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} /> : null}
      </div>
    );
  };

  const legalInstrumentBodyColumn = rowData => {
    return (
      <div onClick={e => e.stopPropagation()}>
        {renderRedirectText(
          rowData.legalInstrument.alias,
          `https://rod.eionet.europa.eu/instruments/${rowData.legalInstrument.id}`
        )}
      </div>
    );
  };

  const obligationBodyColumn = rowData => {
    return (
      <div onClick={e => e.stopPropagation()}>
        {renderRedirectText(
          rowData.obligation.title,
          `https://rod.eionet.europa.eu/obligations/${rowData.obligation.obligationId}`
        )}
      </div>
    );
  };
  const onLoadPublicCountryInformation = async () => {
    try {
      const publicData = [
        {
          id: 1,
          dataflowName: 'Dataflow 1',
          obligation: {
            obligationId: 693,
            title: '(B) Preliminary  information on zones and agglomerations  (Article 6)'
          },
          legalInstrument: {
            alias: 'Air Quality Directive IPR',
            id: 650,
            title: 'obligation 1'
          },
          status: 'open',
          releaseDate: '2021-02-26 10:02',
          isReleased: true,
          publicsFileName: ['Austria-Schema 1']
        },
        {
          id: 1,
          dataflowName: 'Dataflow 2',
          obligation: {
            obligationId: 693,
            title: '(B) Preliminary  information on zones and agglomerations  (Article 6)'
          },
          legalInstrument: {
            alias: 'Air Quality Directive IPR',
            id: 650,
            title: 'obligation 1'
          },
          status: 'closed',
          releaseDate: '2021-01-26 10:02',
          isReleased: true,
          publicsFileName: ['France-Schema 1', 'France-Schema 2', 'France-Schema 3']
        },
        {
          id: 1,
          dataflowName: 'Dataflow 1',
          obligation: {
            obligationId: 693,
            title: '(B) Preliminary  information on zones and agglomerations  (Article 6)'
          },
          legalInstrument: {
            alias: 'Air Quality Directive IPR',
            id: 650,
            title: 'obligation 1'
          },
          status: 'open',
          releaseDate: '2021-02-26 10:02',
          isReleased: true,
          publicsFileName: ['Austria-Schema 1']
        },
        {
          id: 1,
          dataflowName: 'Dataflow 2',
          obligation: {
            obligationId: 693,
            title: '(B) Preliminary  information on zones and agglomerations  (Article 6)'
          },
          legalInstrument: {
            alias: 'Air Quality Directive IPR',
            id: 650,
            title: 'obligation 1'
          },
          status: 'closed',
          releaseDate: '2021-01-26 10:02',
          isReleased: true,
          publicsFileName: ['France-Schema 1', 'France-Schema 2', 'France-Schema 3']
        },
        {
          id: 1,
          dataflowName: 'Dataflow 1',
          obligation: {
            obligationId: 693,
            title: '(B) Preliminary  information on zones and agglomerations  (Article 6)'
          },
          legalInstrument: {
            alias: 'Air Quality Directive IPR',
            id: 650,
            title: 'obligation 1'
          },
          status: 'open',
          releaseDate: '2021-02-26 10:02',
          isReleased: true,
          publicsFileName: ['Austria-Schema 1']
        },
        {
          id: 1,
          dataflowName: 'Dataflow 2',
          obligation: {
            obligationId: 693,
            title: '(B) Preliminary  information on zones and agglomerations  (Article 6)'
          },
          legalInstrument: {
            alias: 'Air Quality Directive IPR',
            id: 650,
            title: 'obligation 1'
          },
          status: 'closed',
          releaseDate: '2021-01-26 10:02',
          isReleased: true,
          publicsFileName: ['France-Schema 1', 'France-Schema 2', 'France-Schema 3']
        },
        {
          id: 1,
          dataflowName: 'Dataflow 1',
          obligation: {
            obligationId: 693,
            title: '(B) Preliminary  information on zones and agglomerations  (Article 6)'
          },
          legalInstrument: {
            alias: 'Air Quality Directive IPR',
            id: 650,
            title: 'obligation 1'
          },
          status: 'open',
          releaseDate: '2021-02-26 10:02',
          isReleased: true,
          publicsFileName: ['Austria-Schema 1']
        },
        {
          id: 1,
          dataflowName: 'Dataflow 2',
          obligation: {
            obligationId: 693,
            title: '(B) Preliminary  information on zones and agglomerations  (Article 6)'
          },
          legalInstrument: {
            alias: 'Air Quality Directive IPR',
            id: 650,
            title: 'obligation 1'
          },
          status: 'closed',
          releaseDate: '2021-01-26 10:02',
          isReleased: true,
          publicsFileName: ['France-Schema 1', 'France-Schema 2', 'France-Schema 3']
        },
        {
          id: 1,
          dataflowName: 'Dataflow 1',
          obligation: {
            obligationId: 693,
            title: '(B) Preliminary  information on zones and agglomerations  (Article 6)'
          },
          legalInstrument: {
            alias: 'Air Quality Directive IPR',
            id: 650,
            title: 'obligation 1'
          },
          status: 'open',
          releaseDate: '2021-02-26 10:02',
          isReleased: true,
          publicsFileName: ['Austria-Schema 1']
        },
        {
          id: 1,
          dataflowName: 'Dataflow 2',
          obligation: {
            obligationId: 693,
            title: '(B) Preliminary  information on zones and agglomerations  (Article 6)'
          },
          legalInstrument: {
            alias: 'Air Quality Directive IPR',
            id: 650,
            title: 'obligation 1'
          },
          status: 'closed',
          releaseDate: '2021-01-26 10:02',
          isReleased: true,
          publicsFileName: ['France-Schema 1', 'France-Schema 2', 'France-Schema 3']
        },
        {
          id: 1,
          dataflowName: 'Dataflow 1',
          obligation: {
            obligationId: 693,
            title: '(B) Preliminary  information on zones and agglomerations  (Article 6)'
          },
          legalInstrument: {
            alias: 'Air Quality Directive IPR',
            id: 650,
            title: 'obligation 1'
          },
          status: 'open',
          releaseDate: '2021-02-26 10:02',
          isReleased: true,
          publicsFileName: ['Austria-Schema 1']
        },
        {
          id: 1,
          dataflowName: 'Dataflow 2',
          obligation: {
            obligationId: 693,
            title: '(B) Preliminary  information on zones and agglomerations  (Article 6)'
          },
          legalInstrument: {
            alias: 'Air Quality Directive IPR',
            id: 650,
            title: 'obligation 1'
          },
          status: 'closed',
          releaseDate: '2021-01-26 10:02',
          isReleased: true,
          publicsFileName: ['France-Schema 1', 'France-Schema 2', 'France-Schema 3']
        }
      ];
      setPublicCountryInformation(publicData);
    } catch (error) {
      console.error('error', error);
    } finally {
      setIsLoading(false);
    }
  };

  const renderColumns = countries => {
    const fieldColumns = Object.keys(countries[0])
      .filter(key => !key.includes('id'))
      .map(field => {
        let template = null;
        if (field === 'isReleased') template = isReleasedBodyColumn;
        if (field === 'legalInstrument') template = legalInstrumentBodyColumn;
        if (field === 'obligation') template = obligationBodyColumn;
        if (field === 'publicsFileName') template = downloadFileBodyColumn;
        return <Column body={template} field={field} header={resources.messages[field]} key={field} sortable={true} />;
      });

    return fieldColumns;
  };

  const renderRedirectText = (text, url) => (
    <a href={url} target="_blank" title={text}>
      <span>
        {text} <FontAwesomeIcon aria-hidden={false} className="p-breadcrumb-home" icon={AwesomeIcons('externalLink')} />
      </span>
    </a>
  );

  return (
    <PublicLayout>
      <div className={styles.content} style={contentStyles}>
        <div className={`rep-container ${styles.repContainer}`}>
          <h1 className={styles.title}>Countries</h1>
          <div className={styles.countriesList}>
            {!isLoading ? (
              <DataTable
                autoLayout={true}
                totalRecords={publicCountryInformation.length}
                value={publicCountryInformation}>
                {renderColumns(publicCountryInformation)}
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
