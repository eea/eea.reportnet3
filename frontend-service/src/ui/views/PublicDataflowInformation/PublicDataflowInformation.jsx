import React, { Fragment, useContext, useEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';
import ReactTooltip from 'react-tooltip';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniq from 'lodash/uniq';
import uniqBy from 'lodash/uniqBy';

import { config } from 'conf';

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
    const [representatives, setRepresentatives] = useState({});

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

    const getPublicFileName = fileName => {
      const splittedFileName = fileName.split('-');
      return splittedFileName[1];
    };

    const downloadFileBodyColumn = rowData => {
      if (!rowData.restrictFromPublic) {
        return (
          <div className={styles.filesContainer}>
            {rowData.publicsFileName.map(publicFileName => (
              <span
                className={styles.downloadIcon}
                onClick={() => onFileDownload(rowData.dataProviderId, publicFileName)}>
                <FontAwesomeIcon icon={AwesomeIcons('7z')} data-tip data-for={publicFileName} />
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
              icon={AwesomeIcons('lock')}
              data-tip
              data-for={'restrictFromPublicField'}
            />
            <ReactTooltip className={styles.tooltipClass} effect="solid" id={'restrictFromPublicField'} place="top">
              <span>{resources.messages['restrictFromPublicField']}</span>
            </ReactTooltip>
          </div>
        );
      }
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

    const isReleasedBodyColumn = rowData => (
      <div className={styles.checkedValueColumn}>
        {rowData.isReleased ? <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} /> : null}
      </div>
    );

    const onFileDownload = async (dataProviderId, fileName) => {
      try {
        const fileContent = await DatasetService.downloadDatasetFileData(dataflowId, dataProviderId, fileName);

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
      } catch (error) {
        console.error('error', error);
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

    return (
      <PublicLayout>
        <div className={`${styles.container} ${isLoading ? styles.isLoading : ''} rep-container`} style={contentStyles}>
          {!isLoading ? (
            !isEmpty(representatives) ? (
              <Fragment>
                <Title icon={'clone'} iconSize={'4rem'} subtitle={dataflowData.description} title={dataflowData.name} />
                <DataTable autoLayout={true} totalRecords={representatives.length} value={representatives}>
                  {renderColumns(representatives)}
                </DataTable>
                <div className={styles.tableLegendContainer}>
                  <span>*</span>
                  <FontAwesomeIcon className={styles.tableLegendIcon} icon={AwesomeIcons('lock')} />
                  <div className={styles.tableLegendText}> {resources.messages['restrictFromPublicField']}</div>
                </div>
              </Fragment>
            ) : (
              <div className={styles.noDatasets}>{resources.messages['noDatasets']}</div>
            )
          ) : (
            <Spinner style={{ top: 0, left: 0 }} />
          )}
        </div>
      </PublicLayout>
    );
  }
);
