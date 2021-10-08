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

import { Button } from 'views/_components/Button';
import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';
import { DownloadFile } from 'views/_components/DownloadFile';
import { PublicLayout } from 'views/_components/Layout/PublicLayout';
import { Spinner } from 'views/_components/Spinner';
import { Title } from 'views/_components/Title';
import { Toolbar } from 'views/_components/Toolbar';

import { DataflowService } from 'services/DataflowService';
import { DatasetService } from 'services/DatasetService';
import { DocumentService } from 'services/DocumentService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { ThemeContext } from 'views/_functions/Contexts/ThemeContext';

import { useBreadCrumbs } from 'views/_functions/Hooks/useBreadCrumbs';

import { CurrentPage } from 'views/_functions/Utils';
import { DataflowUtils } from 'services/_utils/DataflowUtils';
import { FileUtils } from 'views/_functions/Utils/FileUtils';
import { TextByDataflowTypeUtils } from 'views/_functions/Utils/TextByDataflowTypeUtils';
import { TextUtils } from 'repositories/_utils/TextUtils';

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
    const [dataflowData, setDataflowData] = useState({ documents: [], referenceDatasets: [], type: '', webLinks: [] });
    const [isDownloading, setIsDownloading] = useState(false);
    const [isLoading, setIsLoading] = useState(true);
    const [isWrongUrlDataflowId, setIsWrongUrlDataflowId] = useState(false);
    const [representatives, setRepresentatives] = useState({});

    const { documents, referenceDatasets, type: dataflowType, webLinks } = dataflowData;

    const isBusinessDataflow = dataflowType === config.dataflowType.BUSINESS.value;

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

    const getCountryCode = dataProviderName => {
      return Object.values(config.countriesByGroup)
        .flat()
        .find(country => country.name === dataProviderName).code;
    };

    const getHeader = fieldHeader => {
      switch (fieldHeader) {
        case 'dataProviderName':
          return TextByDataflowTypeUtils.getLabelByDataflowType(
            resourcesContext.messages,
            dataflowType,
            'publicDataflowDataProviderNameColumnHeader'
          );
        case 'publicsFileName':
          return resourcesContext.messages['files'];
        default:
          return resourcesContext.messages[fieldHeader];
      }
    };

    const getReferenceDatasetsHeader = fieldHeader => {
      switch (fieldHeader) {
        case 'dataProviderName':
          return resourcesContext.messages['name'];
        case 'publicFileName':
          return resourcesContext.messages['file'];
        default:
          return resourcesContext.messages[fieldHeader];
      }
    };

    const getOrderedRepresentativeColumns = representatives => {
      const representativesWithPriority = [
        { id: 'id', index: 0 },
        { id: 'dataProviderName', index: 1 },
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

    const getOrderedDocumentsColumns = documents => {
      const documentsOrder = [
        { id: 'id', index: 0 },
        { id: 'title', index: 1 },
        { id: 'description', index: 2 },
        { id: 'category', index: 3 },
        { id: 'language', index: 4 },
        { id: 'isPublic', index: 5 },
        { id: 'uploadDate', index: 6 },
        { id: 'size', index: 7 },
        { id: 'file', index: 8 }
      ];

      return documents
        .map(field => documentsOrder.filter(e => field === e.id))
        .flat()
        .sort((a, b) => a.index - b.index)
        .map(orderedField => orderedField.id);
    };

    const dataProviderNameBodyColumn = rowData => (
      <div onClick={e => e.stopPropagation()}>
        <span className={styles.cellWrapper}>
          {rowData.dataProviderName}
          {TextUtils.areEquals(dataflowType, config.dataflowType.REPORTING.value) && (
            <Fragment>
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
                      { countryCode: getCountryCode(rowData.dataProviderName) },
                      true
                    )
                  );
                }}
              />
              <ReactTooltip
                border={true}
                className={styles.tooltipClass}
                effect="solid"
                id="navigateTooltip"
                place="top">
                <span>{resourcesContext.messages['navigateToCountry']}</span>
              </ReactTooltip>
            </Fragment>
          )}
        </span>
      </div>
    );

    const onDownloadAllSchemasInfo = async () => {
      try {
        setIsDownloading(true);
        await DataflowService.generatePublicAllSchemasInfoFile(dataflowId);
      } catch (error) {
        console.error('DatasetSchema - onDownloadAllSchemasInfo .', error);
        notificationContext.add({ type: 'GENERATE_SCHEMAS_INFO_FILE_ERROR' });
      } finally {
        setIsDownloading(false);
      }
    };

    const onDownloadDocument = async document => {
      try {
        const { data } = await DocumentService.publicDownload(document.id, dataflowId);
        if (!isNil(data)) DownloadFile(data, document.file);
      } catch (error) {
        console.error('PublicDataflowInformation - onDownloadDocument.', error);
      }
    };

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
        setDataflowData(data);
        setPublicInformation(data.datasets, data.manualAcceptance);
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
          dataProviderName: datasetSchemaName,
          dataProviderId: dataset.dataProviderId,
          dataflowType: dataflowType,
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

    const renderRepresentativeColumns = representatives => {
      const fieldColumns = getOrderedRepresentativeColumns(Object.keys(representatives[0]))
        .filter(
          key =>
            key.includes('dataProviderName') ||
            key.includes('publicsFileName') ||
            key.includes('deliveryDate') ||
            key.includes('deliveryStatus')
        )
        .map(field => {
          let template = null;
          if (field === 'dataProviderName') template = dataProviderNameBodyColumn;
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
        .filter(key => key.includes('dataProviderName') || key.includes('publicFileName'))
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

    const sizeColumnTemplate = rowData => {
      const formatedRowData = FileUtils.formatBytes(rowData.size);
      return (
        <Fragment>
          {formatedRowData.bytesParsed} {formatedRowData.sizeType}
        </Fragment>
      );
    };

    const downloadDocumentColumnTemplate = rowData => {
      return (
        <div className={styles.filesContainer}>
          <span className={styles.downloadIcon} key={rowData.file} onClick={() => onDownloadDocument(rowData)}>
            <FontAwesomeIcon data-for={rowData.file} data-tip icon={AwesomeIcons('7z')} />
            <ReactTooltip border={true} className={styles.tooltipClass} effect="solid" id={rowData.file} place="top">
              <span>{rowData.file}</span>
            </ReactTooltip>
          </span>
        </div>
      );
    };

    const linkTemplate = rowData => (
      <a href={rowData.url} rel="noopener noreferrer" target="_blank">
        {rowData.url}
      </a>
    );

    const renderDocumentsColumns = documents => {
      const documentsWithFiles = documents.map(document => {
        document['file'] = document.title;
        return document;
      });
      const fieldColumns = getOrderedDocumentsColumns(Object.keys(documentsWithFiles[0]))
        .filter(
          key =>
            key.includes('category') ||
            key.includes('date') ||
            key.includes('description') ||
            key.includes('language') ||
            key.includes('size') ||
            key.includes('file') ||
            key.includes('title')
        )
        .map(field => {
          let template = null;
          if (field === 'size') template = sizeColumnTemplate;
          if (field === 'file') template = downloadDocumentColumnTemplate;
          return (
            <Column
              body={template}
              className={field === 'publicFileName' ? styles.downloadReferenceDatasetFile : ''}
              field={field}
              header={resourcesContext.messages[field]}
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
          let template = null;
          if (field === 'url') template = linkTemplate;
          return (
            <Column body={template} field={field} header={resourcesContext.messages[field]} key={field} sortable />
          );
        });

      return fieldColumns;
    };

    const renderDataflowInformationContent = () => {
      if (isLoading) {
        return <Spinner className={styles.isLoading} />;
      }

      if (isWrongUrlDataflowId) {
        return <div className={styles.noDatasets}>{resourcesContext.messages['wrongUrlDataflowId']}</div>;
      }

      if (isEmpty(representatives) && !isBusinessDataflow) {
        return <div className={styles.noDatasets}>{resourcesContext.messages['noDatasets']}</div>;
      }

      return (
        <Fragment>
          <Title icon="clone" iconSize="4rem" subtitle={dataflowData.description} title={dataflowData.name} />
          <Toolbar className={styles.actionsToolbar}>
            <div className={'p-toolbar-group-left'}>
              <Button
                className={`p-button-rounded p-button-secondary ${!isDownloading ? 'p-button-animated-blink' : ''}`}
                disabled={isDownloading}
                icon={isDownloading ? 'spinnerAnimate' : 'export'}
                label={resourcesContext.messages['downloadSchemasInfo']}
                onClick={() => onDownloadAllSchemasInfo()}
                tooltip={resourcesContext.messages['downloadSchemasInfoTooltip']}
                tooltipOptions={{ position: 'right' }}
              />
            </div>
          </Toolbar>

          {!isEmpty(representatives) && (
            <div className={styles.dataTableWrapper}>
              <div className={styles.dataTableTitle}>{resourcesContext.messages['reportingDatasets']}</div>
              <DataTable autoLayout totalRecords={representatives.length} value={representatives}>
                {renderRepresentativeColumns(representatives)}
              </DataTable>
            </div>
          )}
          {!isEmpty(referenceDatasets) && (
            <div className={styles.dataTableWrapper}>
              <div className={styles.dataTableTitle}>{resourcesContext.messages['referenceDatasets']}</div>
              <DataTable autoLayout totalRecords={referenceDatasets.length} value={referenceDatasets}>
                {renderReferenceDatasetsColumns(referenceDatasets)}
              </DataTable>
            </div>
          )}
          {!isEmpty(documents) && (
            <div className={styles.dataTableWrapper}>
              <div className={styles.dataTableTitle}>{resourcesContext.messages['documents']}</div>
              <DataTable autoLayout totalRecords={documents.length} value={documents}>
                {renderDocumentsColumns(documents)}
              </DataTable>
            </div>
          )}
          {!isEmpty(webLinks) && (
            <div className={styles.dataTableWrapper}>
              <div className={styles.dataTableTitle}>{resourcesContext.messages['webLinks']}</div>
              <DataTable autoLayout totalRecords={webLinks.length} value={webLinks}>
                {renderWebLinksColumns(webLinks)}
              </DataTable>
            </div>
          )}
        </Fragment>
      );
    };

    return (
      <PublicLayout>
        <div className={`${styles.container} rep-container`} style={contentStyles}>
          {renderDataflowInformationContent()}
        </div>
      </PublicLayout>
    );
  }
);
