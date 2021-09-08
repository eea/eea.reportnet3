import { Fragment, useContext, useEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';
import ReactTooltip from 'react-tooltip';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniq from 'lodash/uniq';

import { config } from 'conf';
import { getUrl } from 'repositories/_utils/UrlUtils';
import { routes } from 'conf/routes';

import styles from './PublicDataflowInformation.module.scss';

import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';
import { DownloadFile } from 'views/_components/DownloadFile';
import { PublicLayout } from 'views/_components/Layout/PublicLayout';
import { Spinner } from 'views/_components/Spinner';
import { Title } from 'views/_components/Title';

import { DataflowService } from 'services/DataflowService';
import { DatasetService } from 'services/DatasetService';
import { DocumentService } from 'services/DocumentService';
import { WebLinkService } from 'services/WebLinkService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { ThemeContext } from 'views/_functions/Contexts/ThemeContext';

import { useBreadCrumbs } from 'views/_functions/Hooks/useBreadCrumbs';

import { CurrentPage } from 'views/_functions/Utils';
import { DataflowUtils } from 'services/_utils/DataflowUtils';

export const PublicDataflowInformation = withRouter(
  ({
    history,
    match: {
      params: { dataflowId }
    }
  }) => {
    const resourcesContext = useContext(ResourcesContext);
    const themeContext = useContext(ThemeContext);
    const notificationContext = useContext(NotificationContext);

    const [contentStyles, setContentStyles] = useState({});
    const [dataflowData, setDataflowData] = useState({});
    const [documents, setDocuments] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isWrongUrlDataflowId, setIsWrongUrlDataflowId] = useState(false);
    const [referenceDatasets, setReferenceDatasets] = useState([]);
    const [representatives, setRepresentatives] = useState({});
    const [webLinks, setWebLinks] = useState([]);

    useBreadCrumbs({ currentPage: CurrentPage.PUBLIC_DATAFLOW, dataflowId, history });

    useEffect(() => {
      onLoadPublicDataflowInformation();
    }, []);

    useEffect(() => {
      if (!themeContext.headerCollapse) {
        setContentStyles({ marginTop: `${config.theme.cookieConsentHeight + 6}px` });
      } else {
        setContentStyles({});
      }
    }, [themeContext.headerCollapse]);

    const downloadFileBodyColumn = rowData => {
      if (!rowData.restrictFromPublic) {
        return (
          <div className={styles.filesContainer}>
            {rowData.publicsFileName.map(publicFileName => (
              <span
                className={styles.downloadIcon}
                key={publicFileName}
                onClick={() => onFileDownload(rowData.dataProviderId, publicFileName)}>
                <FontAwesomeIcon data-for={publicFileName} data-tip icon={AwesomeIcons('7z')} />
                <ReactTooltip
                  border={true}
                  className={styles.tooltipClass}
                  effect="solid"
                  id={publicFileName}
                  place="top">
                  <span>{publicFileName.split('-')[1]}</span>
                </ReactTooltip>
              </span>
            ))}
          </div>
        );
      } else {
        return (
          <div className={styles.filesContainer}>
            <FontAwesomeIcon
              className={styles.restrictFromPublicIcon}
              data-for={'restrictFromPublicField'}
              data-tip
              icon={AwesomeIcons('lock')}
            />
            <ReactTooltip
              border={true}
              className={styles.tooltipClass}
              effect="solid"
              id={'restrictFromPublicField'}
              place="top">
              <span>{resourcesContext.messages['restrictFromPublicField']}</span>
            </ReactTooltip>
          </div>
        );
      }
    };

    const downloadReferenceDatasetFileBodyColumn = rowData => {
      if (!isNil(rowData.publicFileName)) {
        return (
          <span
            className={styles.downloadIcon}
            key={rowData.publicFileName}
            onClick={() => onFileDownload(null, rowData.publicFileName)}>
            <FontAwesomeIcon data-for={rowData.publicFileName} data-tip icon={AwesomeIcons('7z')} />
            <ReactTooltip
              border={true}
              className={styles.tooltipClass}
              effect="solid"
              id={rowData.publicFileName}
              place="top">
              <span>{rowData.publicFileName}</span>
            </ReactTooltip>
          </span>
        );
      }
    };

    const getCountryCode = datasetSchemaName => {
      return Object.values(config.countriesByGroup)
        .flat()
        .find(country => country.name === datasetSchemaName).code;
    };

    const getHeader = fieldHeader => {
      switch (fieldHeader) {
        case 'datasetSchemaName':
          return resourcesContext.messages['countries'];
        case 'publicsFileName':
          return resourcesContext.messages['files'];
        default:
          return resourcesContext.messages[fieldHeader];
      }
    };

    const getReferenceDatasetsHeader = fieldHeader => {
      switch (fieldHeader) {
        case 'datasetSchemaName':
          return resourcesContext.messages['name'];
        case 'publicFileName':
          return resourcesContext.messages['file'];
        default:
          return resourcesContext.messages[fieldHeader];
      }
    };

    const getDocumentsHeader = fieldHeader => {
      switch (fieldHeader) {
        case 'datasetSchemaName':
          return resourcesContext.messages['name'];
        case 'publicFileName':
          return resourcesContext.messages['file'];
        default:
          return resourcesContext.messages[fieldHeader];
      }
    };

    const getWebLinksHeader = fieldHeader => {
      switch (fieldHeader) {
        case 'datasetSchemaName':
          return resourcesContext.messages['name'];
        case 'publicFileName':
          return resourcesContext.messages['file'];
        default:
          return resourcesContext.messages[fieldHeader];
      }
    };

    const getOrderedColumns = representatives => {
      const representativesWithPriority = [
        { id: 'id', index: 0 },
        { id: 'datasetSchemaName', index: 1 },
        { id: 'deliveryDate', index: 2 },
        { id: 'deliveryStatus', index: 3 },
        { id: 'publicsFileName', index: 4 }
      ];

      return representatives
        .map(field => representativesWithPriority.filter(e => field === e.id))
        .flat()
        .sort((a, b) => a.index - b.index)
        .map(orderedField => orderedField.id);
    };

    const countryBodyColumn = rowData => (
      <div onClick={e => e.stopPropagation()}>
        <span className={styles.cellWrapper}>
          {rowData.datasetSchemaName}
          <FontAwesomeIcon
            aria-hidden={false}
            className={`p-breadcrumb-home ${styles.link}`}
            data-for="navigateTooltip"
            data-tip
            icon={AwesomeIcons('externalUrl')}
            onClick={e => {
              e.preventDefault();
              history.push(
                getUrl(
                  routes.PUBLIC_COUNTRY_INFORMATION,
                  { countryCode: getCountryCode(rowData.datasetSchemaName) },
                  true
                )
              );
            }}
          />
          <ReactTooltip border={true} className={styles.tooltipClass} effect="solid" id="navigateTooltip" place="top">
            <span>{resourcesContext.messages['navigateToCountry']}</span>
          </ReactTooltip>
        </span>
      </div>
    );

    const onFileDownload = async (dataProviderId, fileName) => {
      try {
        let fileContent;

        if (!isNil(dataProviderId)) {
          fileContent = await DatasetService.downloadPublicDatasetFile(dataflowId, dataProviderId, fileName);
        } else {
          fileContent = await DatasetService.downloadPublicReferenceDatasetFileData(dataflowId, fileName);
        }
        DownloadFile(fileContent.data, fileName);
      } catch (error) {
        console.error('PublicDataflowInformation - onFileDownload.', error);
        if (error.response.status === 404) {
          notificationContext.add({
            type: 'DOWNLOAD_DATASET_FILE_NOT_FOUND_EVENT'
          });
        } else {
          notificationContext.add({
            type: 'DOWNLOAD_DATASET_FILE_ERROR'
          });
        }
      }
    };

    const onLoadPublicDataflowInformation = async () => {
      try {
        const data = await DataflowService.getPublicDataflowData(dataflowId);
        console.log('data', data);
        setDataflowData(data);
        setPublicInformation(data.datasets, data.manualAcceptance);
        setReferenceDatasets(data.referenceDatasets);
        setDocuments(data.documents);
        setWebLinks(data.webLinks);
      } catch (error) {
        console.error('PublicDataflowInformation - onLoadDataflowData.', error);
        if (error.response.status === 404 || error.response.status === 400) {
          setIsWrongUrlDataflowId(true);
        } else {
          notificationContext.add({ type: 'LOAD_DATAFLOW_INFO_ERROR' });
        }
      } finally {
        setIsLoading(false);
      }
    };

    const setPublicInformation = (datasets, hasManualAcceptance) => {
      if (isNil(datasets)) return [];

      const datasetsSchemaName = uniq(datasets.map(dataset => dataset.datasetSchemaName));
      const representatives = datasetsSchemaName.map(datasetSchemaName => {
        const datasetsFromRepresentative = datasets.filter(dataset => dataset.datasetSchemaName === datasetSchemaName);
        const dataset = datasetsFromRepresentative[0];
        const publicFileNames = datasetsFromRepresentative
          .filter(dataset => !isNil(dataset.publicFileName))
          .map(dataset => dataset.publicFileName);

        return {
          datasetSchemaName: datasetSchemaName,
          dataProviderId: dataset.dataProviderId,
          deliveryDate: dataset.releaseDate,
          restrictFromPublic: dataset.restrictFromPublic,
          publicsFileName: publicFileNames,
          deliveryStatus: !dataset.isReleased
            ? config.datasetStatus.PENDING.label
            : !hasManualAcceptance
            ? config.datasetStatus.DELIVERED.label
            : DataflowUtils.getTechnicalAcceptanceStatus(datasetsFromRepresentative.map(dataset => dataset.status))
        };
      });

      setRepresentatives(representatives);
    };

    const renderColumns = representatives => {
      const fieldColumns = getOrderedColumns(Object.keys(representatives[0]))
        .filter(
          key =>
            key.includes('datasetSchemaName') ||
            key.includes('publicsFileName') ||
            key.includes('deliveryDate') ||
            key.includes('deliveryStatus')
        )
        .map(field => {
          let template = null;
          if (field === 'datasetSchemaName') template = countryBodyColumn;
          if (field === 'publicsFileName') template = downloadFileBodyColumn;
          return (
            <Column
              body={template}
              className={field === 'publicsFileName' ? styles.downloadFile : ''}
              field={field}
              header={getHeader(field)}
              key={field}
              sortable={field === 'publicsFileName' ? false : true}
            />
          );
        });

      return fieldColumns;
    };

    const renderReferenceDatasetsColumns = referenceDatasets => {
      const fieldColumns = Object.keys(referenceDatasets[0])
        .filter(key => key.includes('datasetSchemaName') || key.includes('publicFileName'))
        .map(field => {
          let template = null;
          if (field === 'publicFileName') template = downloadReferenceDatasetFileBodyColumn;
          return (
            <Column
              body={template}
              className={field === 'publicFileName' ? styles.downloadReferenceDatasetFile : ''}
              field={field}
              header={getReferenceDatasetsHeader(field)}
              key={field}
              sortable={field === 'publicFileName' ? false : true}
            />
          );
        });

      return fieldColumns;
    };

    const renderDocumentsColumns = documents => {
      const fieldColumns = Object.keys(documents[0])
        .filter(
          key =>
            key.includes('category') ||
            key.includes('date') ||
            key.includes('description') ||
            key.includes('language') ||
            key.includes('size') ||
            key.includes('title')
        )
        .map(field => {
          //let template = null;
          //if (field === 'publicFileName') template = downloadReferenceDatasetFileBodyColumn;
          return (
            <Column
              //body={template}
              //className={field === 'publicFileName' ? styles.downloadReferenceDatasetFile : ''}
              field={field}
              header={getDocumentsHeader(field)}
              key={field}
              sortable
            />
          );
        });

      return fieldColumns;
    };

    const renderWebLinksColumns = webLinks => {
      const fieldColumns = Object.keys(webLinks[0])
        .filter(key => key.includes('description') || key.includes('url'))
        .map(field => {
          //let template = null;
          //if (field === 'publicFileName') template = downloadReferenceDatasetFileBodyColumn;
          return (
            <Column
              //body={template}
              //className={field === 'publicFileName' ? styles.downloadReferenceDatasetFile : ''}
              field={field}
              header={getWebLinksHeader(field)}
              key={field}
              sortable
            />
          );
        });

      return fieldColumns;
    };

    return (
      <PublicLayout>
        <div className={`${styles.container} rep-container`} style={contentStyles}>
          {!isLoading ? (
            isWrongUrlDataflowId ? (
              <div className={styles.noDatasets}>{resourcesContext.messages['wrongUrlDataflowId']}</div>
            ) : !isEmpty(representatives) ? (
              <Fragment>
                <Title icon={'clone'} iconSize={'4rem'} subtitle={dataflowData.description} title={dataflowData.name} />
                <DataTable autoLayout={true} totalRecords={representatives.length} value={representatives}>
                  {renderColumns(representatives)}
                </DataTable>
                {!isEmpty(referenceDatasets) && (
                  <div className={styles.dataTableWrapper}>
                    <div className={styles.dataTableTitle}>{resourcesContext.messages['referenceDatasets']}</div>
                    <DataTable autoLayout={true} totalRecords={referenceDatasets.length} value={referenceDatasets}>
                      {renderReferenceDatasetsColumns(referenceDatasets)}
                    </DataTable>
                  </div>
                )}
                {!isEmpty(documents) && (
                  <div className={styles.dataTableWrapper}>
                    <div className={styles.dataTableTitle}>{resourcesContext.messages['documents']}</div>
                    <DataTable autoLayout={true} totalRecords={documents.length} value={documents}>
                      {renderDocumentsColumns(documents)}
                    </DataTable>
                  </div>
                )}
                {!isEmpty(webLinks) && (
                  <div className={styles.dataTableWrapper}>
                    <div className={styles.dataTableTitle}>{resourcesContext.messages['webLinks']}</div>
                    <DataTable autoLayout={true} totalRecords={webLinks.length} value={webLinks}>
                      {renderWebLinksColumns(webLinks)}
                    </DataTable>
                  </div>
                )}
              </Fragment>
            ) : (
              <div className={styles.noDatasets}>{resourcesContext.messages['noDatasets']}</div>
            )
          ) : (
            <Spinner className={styles.isLoading} />
          )}
        </div>
      </PublicLayout>
    );
  }
);
