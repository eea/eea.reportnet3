import React, { useContext, useEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './PublicDataflowInformation.module.scss';

import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { PublicLayout } from 'ui/views/_components/Layout/PublicLayout';
import { Spinner } from 'ui/views/_components/Spinner';
import { Title } from 'ui/views/_components/Title';

import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const PublicDataflowInformation = withRouter(
  ({
    history,
    match: {
      params: { dataflowId }
    }
  }) => {
    const resources = useContext(ResourcesContext);

    const [dataflowData, setDataflowData] = useState({});
    const [isLoading, setIsLoading] = useState(true);

    const { datasets } = dataflowData;

    useEffect(() => {
      onLoadDataflowData();
    }, []);

    const exportableFileBodyColumn = rowData => (
      <div
        className={styles.downloadIcon}
        onClick={() => onFileDownload(rowData.dataProviderId, rowData.exportableFile)}>
        {!isNil(rowData.exportableFile) && (
          <FontAwesomeIcon icon={AwesomeIcons(getFileExtension(rowData.exportableFile))} />
        )}
      </div>
    );

    const getFileExtension = file => {
      const splittedFile = file.split('.');
      return splittedFile[splittedFile.length - 1];
    };

    const getHeader = fieldHeader => {
      let header;
      switch (fieldHeader) {
        case 'datasetSchemaName':
          header = resources.messages['countries'];
          break;
        case 'name':
          header = resources.messages['datasetName'];
          break;
        case 'releaseDate':
          header = resources.messages['releaseDate'];
          break;
        case 'isReleased':
          header = resources.messages['isReleased'];
          break;
        case 'exportableFile':
          header = resources.messages['file'];
          break;
        default:
          break;
      }
      return header;
    };

    const getOrderedColumns = datasets => {
      const datasetsWithPriority = [
        { id: 'id', index: 0 },
        { id: 'datasetSchemaName', index: 1 },
        { id: 'name', index: 2 },
        { id: 'isReleased', index: 3 },
        { id: 'releaseDate', index: 4 },
        { id: 'exportableFile', index: 5 }
      ];

      return datasets
        .map(field => datasetsWithPriority.filter(e => field === e.id))
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
      const fileContent = await DatasetService.downloadDatasetFileData(dataflowId, dataProviderId, fileName);
      // const fileContent = {};

      DownloadFile(fileContent, fileName);
    };

    const onLoadDataflowData = async () => {
      try {
        setDataflowData(await DataflowService.getPublicDataflowData(dataflowId));
      } catch (error) {
        console.error('error', error);
      } finally {
        setIsLoading(false);
      }
    };

    const renderColumns = datasets => {
      const fieldColumns = getOrderedColumns(Object.keys(datasets[0]))
        .filter(
          key =>
            key.includes('datasetSchemaName') ||
            key.includes('name') ||
            key.includes('isReleased') ||
            key.includes('releaseDate') ||
            key.includes('exportableFile')
        )
        .map(field => {
          let template = null;
          if (field === 'isReleased') template = isReleasedBodyColumn;
          if (field === 'exportableFile') template = exportableFileBodyColumn;
          return <Column body={template} field={field} header={getHeader(field)} key={field} sortable={true} />;
        });

      return fieldColumns;
    };

    return (
      <PublicLayout>
        <div className={`${styles.container} rep-container`}>
          <Title icon={'clone'} iconSize={'4rem'} subtitle={dataflowData.description} title={dataflowData.name} />
          {!isLoading ? (
            !isEmpty(datasets) ? (
              <DataTable
                className={styles.dataflowDataTable}
                autoLayout={true}
                paginator={true}
                paginatorRight={
                  <span>
                    {`${resources.messages['totalRecords']}  ${datasets.length}`}{' '}
                    {resources.messages['records'].toLowerCase()}
                  </span>
                }
                rows={10}
                rowsPerPageOptions={[5, 10, 15]}
                totalRecords={datasets.length}
                value={datasets}>
                {renderColumns(datasets)}
              </DataTable>
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
