import React, { useContext, useEffect, useState } from 'react';

import styles from './SnapshotSliderBar.module.css';

import { config } from 'assets/conf';

import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Sidebar } from 'primereact/sidebar';
import { SnapshotContext } from '../../ReporterDataSet';
import { SnapshotList } from './_components/SnapshotList';

import { getUrl } from 'core/infrastructure/getUrl';
import { SnapshotService } from 'core/services/Snapshot';

const SnapshotSlideBar = ({ isVisible, setIsVisible, dataSetId, isSnapshotListRerender }) => {
  const snapshotContext = useContext(SnapshotContext);
  const resources = useContext(ResourcesContext);
  const [snapshotListData, setSnapshotListData] = useState([]);
  const [snapshotDescription, setSnapshotDescription] = useState('');

  const setSnapshotList = async () => {
    setSnapshotListData(await SnapshotService.all(getUrl(config.loadSnapshotsListAPI.url, { dataSetId })));
  };

  useEffect(() => {
    setSnapshotList();
  }, []);

  useEffect(() => {
    setSnapshotList();
  }, [isSnapshotListRerender]);

  return (
    <Sidebar visible={isVisible} onHide={e => setIsVisible()} position="right">
      <div className={styles.content}>
        <div className={styles.title}>
          <h3>{resources.messages.createSnapshotTitle}</h3>
        </div>
        <div className={`${styles.newContainer} ${styles.section}`}>
          <input
            type="text"
            placeholder={resources.messages.createSnapshotPlaceholder}
            onChange={e => {
              setSnapshotDescription(e.target.value);
            }}
          />
          <button
            className="rp-btn primary"
            onClick={() =>
              snapshotContext.snapshotDispatch({
                type: 'create_snapshot',
                payload: {
                  description: snapshotDescription
                }
              })
            }>
            {resources.messages.create}
          </button>
        </div>
        <SnapshotList snapshotListData={snapshotListData} />
      </div>
    </Sidebar>
  );
};

export { SnapshotSlideBar };
