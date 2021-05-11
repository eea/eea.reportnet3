import { Fragment, useContext, useEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';
import ReactTooltip from 'react-tooltip';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniq from 'lodash/uniq';
import uniqBy from 'lodash/uniqBy';

import { config } from 'conf';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { routes } from 'ui/routes';

import styles from './PublicDataflowInformation.module.scss';

import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { PublicLayout } from 'ui/views/_components/Layout/PublicLayout';
import { Spinner } from 'ui/views/_components/Spinner';
import { Title } from 'ui/views/_components/Title';

import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { ThemeContext } from 'ui/views/_functions/Contexts/ThemeContext';

import { useBreadCrumbs } from 'ui/views/_functions/Hooks/useBreadCrumbs';

import { CurrentPage } from 'ui/views/_functions/Utils';

export const PublicDataflowInformation = withRouter(
  ({
    history,
    match: {
      params: { dataflowId }
    }
  }) => {
    const resources = useContext(ResourcesContext);
    const themeContext = useContext(ThemeContext);

    const [contentStyles, setContentStyles] = useState({});
    const [dataflowData, setDataflowData] = useState({});
    const [isLoading, setIsLoading] = useState(true);
    const [referenceDatasets, setReferenceDatasets] = useState([]);
    const [representatives, setRepresentatives] = useState({});
    const [isWrongUrlDataflowId, setIsWrongUrlDataflowId] = useState(false);

    const notificationContext = useContext(NotificationContext);

    useBreadCrumbs({ currentPage: CurrentPage.PUBLIC_DATAFLOW, dataflowId, history });

    useEffect(() => {
      onLoadDataflowData();
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
                <ReactTooltip className={styles.tooltipClass} effect="solid" id={publicFileName} place="top">
                  <span>{getPublicFileName(publicFileName)}</span>
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
            <ReactTooltip className={styles.tooltipClass} effect="solid" id={'restrictFromPublicField'} place="top">
              <span>{resources.messages['restrictFromPublicField']}</span>
            </ReactTooltip>
          </div>
        );
      }
    };

    const downloadReferenceDatasetFileBodyColumn = rowData => {
      !isNil(rowData.publucFileName) && (
        <span
          className={styles.downloadIcon}
          key={rowData.publicFileName}
          onClick={() => onFileDownload(null, rowData.publicFileName)}>
          <FontAwesomeIcon data-for={rowData.publicFileName} data-tip icon={AwesomeIcons('7z')} />
          <ReactTooltip className={styles.tooltipClass} effect="solid" id={rowData.publicFileName} place="top">
            <span>{rowData.publicFileName}</span>
          </ReactTooltip>
        </span>
      );
    };

    const getCountryCode = datasetSchemaName => {
      let country = {};
      if (!isNil(config.countriesByGroup)) {
        const countryFinded = Object.keys(config.countriesByGroup).some(countriesGroup => {
          country = config.countriesByGroup[countriesGroup].find(
            groupCountry => groupCountry.name === datasetSchemaName
          );
          return !isNil(country) ? country : false;
        });
        return countryFinded ? country.code : '';
      }
    };

    const getPublicFileName = fileName => {
      const splittedFileName = fileName.split('-');
      return splittedFileName[1];
    };

    const getHeader = fieldHeader => {
      let header;
      switch (fieldHeader) {
        case 'datasetSchemaName':
          header = resources.messages['countries'];
          break;
        case 'releaseDate':
          header = resources.messages['releaseDate'];
          break;
        case 'isReleased':
          header = resources.messages['delivered'];
          break;
        case 'publicsFileName':
          header = resources.messages['files'];
          break;
        default:
          break;
      }
      return header;
    };

    const getReferenceDatasetsHeader = fieldHeader => {
      console.log(`fieldHeader`, fieldHeader);
      let header;
      switch (fieldHeader) {
        case 'datasetSchemaName':
          header = resources.messages['name'];
          break;
        case 'publicFileName':
          header = resources.messages['file'];
          break;
        default:
          break;
      }
      return header;
    };

    const getOrderedColumns = representatives => {
      const representativesWithPriority = [
        { id: 'id', index: 0 },
        { id: 'datasetSchemaName', index: 1 },
        { id: 'isReleased', index: 2 },
        { id: 'releaseDate', index: 3 },
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
            icon={AwesomeIcons('externalLink')}
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
          <ReactTooltip className={styles.tooltipClass} effect="solid" id="navigateTooltip" place="top">
            <span>{resources.messages['navigateToCountry']}</span>
          </ReactTooltip>
        </span>
      </div>
    );

    const isReleasedBodyColumn = rowData => (
      <div className={styles.checkedValueColumn}>
        {rowData.isReleased ? <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} /> : null}
      </div>
    );

    const onFileDownload = async (dataProviderId, fileName) => {
      try {
        let fileContent;

        if (!isNil(dataProviderId)) {
          fileContent = await DatasetService.downloadDatasetFileData(dataflowId, dataProviderId, fileName);
        } else {
          fileContent = await DatasetService.downloadReferenceDatasetFileData(dataflowId, fileName);
        }
        DownloadFile(fileContent.data, fileName);
      } catch (error) {
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

    const onLoadDataflowData = async () => {
      try {
        const { data } = await DataflowService.getPublicDataflowData(dataflowId);
        setDataflowData(data);
        parseDataflowData(data.datasets);
        setReferenceDatasets(data.referenceDatasets);
      } catch (error) {
        console.error('error', error);
        setIsWrongUrlDataflowId(true);
        notificationContext.add({ type: 'LOAD_DATAFLOW_INFO_ERROR' });
      } finally {
        setIsLoading(false);
      }
    };

    const parseDataflowData = datasets => {
      const parsedDatasets = [];

      const datasetsSchemaName = !isNil(datasets) && uniq(datasets.map(dataset => dataset.datasetSchemaName));

      !isNil(datasets) &&
        datasetsSchemaName.forEach(datasetSchemaName => {
          const publicsFileName = [];
          datasets.forEach(dataset => {
            if (dataset.datasetSchemaName === datasetSchemaName) {
              if (!isNil(dataset.publicFileName)) {
                publicsFileName.push(dataset.publicFileName);
              }
              const parsedDataset = {
                datasetSchemaName: datasetSchemaName,
                dataProviderId: dataset.dataProviderId,
                isReleased: dataset.isReleased,
                releaseDate: dataset.releaseDate,
                restrictFromPublic: dataset.restrictFromPublic,
                publicsFileName: publicsFileName
              };
              parsedDatasets.push(parsedDataset);
            }
          });
        });

      const uniqParsedDatasets = uniqBy(parsedDatasets, 'datasetSchemaName');

      setRepresentatives(uniqParsedDatasets);
    };

    const renderColumns = representatives => {
      const fieldColumns = getOrderedColumns(Object.keys(representatives[0]))
        .filter(
          key =>
            key.includes('datasetSchemaName') ||
            key.includes('isReleased') ||
            key.includes('releaseDate') ||
            key.includes('publicsFileName')
        )
        .map(field => {
          let template = null;
          if (field === 'datasetSchemaName') template = countryBodyColumn;
          if (field === 'isReleased') template = isReleasedBodyColumn;
          if (field === 'publicsFileName') template = downloadFileBodyColumn;
          return (
            <Column
              body={template}
              className={field === 'publicsFileName' && styles.downloadFile}
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
              className={field === 'publicFileName' && styles.downloadFile}
              field={field}
              header={getReferenceDatasetsHeader(field)}
              key={field}
              sortable={field === 'publicFileName' ? false : true}
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
              <div className={styles.noDatasets}>{resources.messages['wrongUrlDataflowId']}</div>
            ) : (
              <Fragment>
                <Title icon={'clone'} iconSize={'4rem'} subtitle={dataflowData.description} title={dataflowData.name} />
                {!isEmpty(representatives) ? (
                  <Fragment>
                    <DataTable autoLayout={true} totalRecords={representatives.length} value={representatives}>
                      {renderColumns(representatives)}
                    </DataTable>
                    <div className={styles.tableLegendContainer}>
                      <span>*</span>
                      <FontAwesomeIcon className={styles.tableLegendIcon} icon={AwesomeIcons('lock')} />
                      <div className={styles.tableLegendText}> {resources.messages['restrictFromPublicField']}</div>
                    </div>
                    {!isEmpty(referenceDatasets) && (
                      <div className={styles.referenceDatasetsWrapper}>
                        <div className={styles.referenceDatasetsTitle}>{resources.messages['referenceDatasets']}</div>
                        <DataTable
                          autoLayout={true}
                          className={styles.referenceDatasetsTable}
                          totalRecords={referenceDatasets.length}
                          value={referenceDatasets}>
                          {renderReferenceDatasetsColumns(referenceDatasets)}
                        </DataTable>
                      </div>
                    )}
                  </Fragment>
                ) : (
                  <div className={styles.noDatasets}>{resources.messages['noDatasets']}</div>
                )}
              </Fragment>
            )
          ) : (
            <Spinner className={styles.isLoading} />
          )}
        </div>
      </PublicLayout>
    );
  }
);
