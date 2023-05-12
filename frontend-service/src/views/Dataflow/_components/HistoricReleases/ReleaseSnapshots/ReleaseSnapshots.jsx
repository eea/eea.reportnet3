import { useContext, useEffect, useState } from 'react';

import ReactTooltip from 'react-tooltip';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './ReleaseSnapshots.module.scss';

import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';
import { Spinner } from 'views/_components/Spinner';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { ReleaseSnapshotsService } from 'services/ReleaseSnapshotsService';

import { DownloadFile } from 'views/_components/DownloadFile';
import { PaginatorRecordsCount } from 'views/_components/DataTable/_functions/Utils/PaginatorRecordsCount';

export const ReleaseSnapshots = ({ dataflowId, dataflowType, dataProviderId, datasetId, historicReleasesView }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [isLoading, setIsLoading] = useState(false);
  const [releaseSnapshotsList, setReleaseSnapshotsList] = useState();

  useEffect(() => {
    getReleaseSnapshots();
  }, []);

  const getReleaseSnapshots = async () => {
    try {
      setIsLoading(true);
      const releaseSnapshots = await ReleaseSnapshotsService.getLatestReleaseSnapshots(dataflowId, datasetId);
      setReleaseSnapshotsList(releaseSnapshots.data);
    } catch (error) {
      console.error('ReleaseSnapshots - getReleaseSnapshots.', error);
      notificationContext.add({ type: 'LOAD_RELEASE_SNAPSHOTS_ERROR' }, true);
    } finally {
      setIsLoading(false);
    }
  };

  const getColumns = () => {
    const columns = [
      {
        key: 'fileName',
        header: resourcesContext.messages['name'],
        template: releaseSnapshotNameTemplate
      },
      {
        key: 'releaseSnapshotFile',
        header: resourcesContext.messages['file'],
        template: releaseSnapshotFileTemplate
      }
    ];

    return columns.map(column => (
      <Column
        body={column.template}
        columnResizeMode="expand"
        field={column.key}
        header={column.header}
        key={column.key}
        sortable={true}
      />
    ));
  };

  const onDownloadFile = async fileName => {
    try {
      const { data } = await ReleaseSnapshotsService.downloadSnapshot(datasetId, dataflowId, fileName);
      if (!isNil(data)) DownloadFile(data, fileName);
    } catch (error) {
      console.error('ReleaseSnapshots - onDownloadFile.', error);
    }
  };

  const releaseSnapshotNameTemplate = rowData => <p>{rowData}</p>;

  const releaseSnapshotFileTemplate = rowData => {
    return (
      <div className={styles.filesContainer}>
        <span className={styles.downloadIcon} key={rowData} onClick={() => onDownloadFile(rowData)}>
          <FontAwesomeIcon data-for={rowData} data-tip icon={AwesomeIcons('7z')} />
          <ReactTooltip border={true} className={styles.tooltipClass} effect="solid" id={rowData} place="top">
            <span>{rowData}</span>
          </ReactTooltip>
        </span>
      </div>
    );
  };

  const renderReleaseSnapshotsTable = () => {
    if (isEmpty(releaseSnapshotsList)) {
      return <div className={styles.emptyFilteredData}>{resourcesContext.messages['noReleaseSnapshots']}</div>;
    }

    return (
      <div className={styles.snapshots}>
        <DataTable
          autoLayout={true}
          className={
            historicReleasesView === 'dataCollection' || historicReleasesView === 'EUDataset' ? '' : styles.noFilters
          }
          paginator={true}
          paginatorRight={<PaginatorRecordsCount dataLength={releaseSnapshotsList ? releaseSnapshotsList.length : 0} />}
          rows={10}
          rowsPerPageOptions={[5, 10, 15]}
          summary={resourcesContext.messages['releaseSnapshots']}
          totalRecords={releaseSnapshotsList ? releaseSnapshotsList.length : 0}
          value={releaseSnapshotsList}>
          {getColumns()}
        </DataTable>
      </div>
    );
  };

  const renderReleaseSnapshotsContent = () => {
    if (isLoading) {
      return (
        <div className={styles.releaseSnapshotsWithoutTable}>
          <div className={styles.spinner}>
            <Spinner className={styles.spinnerPosition} />
          </div>
        </div>
      );
    }

    if (isEmpty(releaseSnapshotsList)) {
      return (
        <div className={styles.releaseSnapshotsWithoutTable}>
          <div className={styles.noReleaseSnapshots}>{resourcesContext.messages['noReleaseSnapshots']}</div>
        </div>
      );
    }

    return <div className={styles.releaseSnapshots}>{renderReleaseSnapshotsTable()}</div>;
  };

  return renderReleaseSnapshotsContent();
};
