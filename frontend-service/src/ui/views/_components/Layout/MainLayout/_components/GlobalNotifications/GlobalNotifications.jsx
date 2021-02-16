import React, { Fragment, useContext, useEffect } from 'react';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';

import { useCheckNotifications } from 'ui/views/_functions/Hooks/useCheckNotifications';

import { MetadataUtils } from 'ui/views/_functions/Utils';

const GlobalNotifications = () => {
  const notificationContext = useContext(NotificationContext);

  const notifyValidateDataInitReporting = async () => {
    const notification = notificationContext.toShow.find(
      notification =>
        notification.key === 'IMPORT_REPORTING_COMPLETED_EVENT' ||
        notification.key === 'EXTERNAL_IMPORT_REPORTING_COMPLETED_EVENT' ||
        notification.key === 'EXTERNAL_IMPORT_REPORTING_FROM_OTHER_SYSTEM_COMPLETED_EVENT' ||
        notification.key === 'DELETE_TABLE_COMPLETED_EVENT' ||
        notification.key === 'DELETE_DATASET_DATA_COMPLETED_EVENT' ||
        notification.key === 'RESTORE_DATASET_SNAPSHOT_COMPLETED_EVENT'
    );

    const dataflowId = notification.content.dataflowId;
    const datasetId = notification.content.datasetId;

    const {
      dataflow: { name: dataflowName },
      dataset: { name: datasetName }
    } = await MetadataUtils.getMetadata({ dataflowId, datasetId });

    notificationContext.add({
      type: 'VALIDATE_DATA_INIT',
      content: {
        countryName: 'REPORTING',
        dataflowId,
        dataflowName,
        datasetId,
        datasetName
      }
    });
  };

  useCheckNotifications(
    [
      'IMPORT_REPORTING_COMPLETED_EVENT',
      'EXTERNAL_IMPORT_REPORTING_COMPLETED_EVENT',
      'EXTERNAL_IMPORT_REPORTING_FROM_OTHER_SYSTEM_COMPLETED_EVENT',
      'DELETE_TABLE_COMPLETED_EVENT',
      'DELETE_DATASET_DATA_COMPLETED_EVENT',
      'RESTORE_DATASET_SNAPSHOT_COMPLETED_EVENT'
    ],
    notifyValidateDataInitReporting
  );

  const notifyValidateDataInitDesign = async () => {
    const notification = notificationContext.toShow.find(
      notification =>
        notification.key === 'IMPORT_DESIGN_COMPLETED_EVENT' ||
        notification.key === 'EXTERNAL_IMPORT_DESIGN_COMPLETED_EVENT' ||
        notification.key === 'EXTERNAL_IMPORT_DESIGN_FROM_OTHER_SYSTEM_COMPLETED_EVENT' ||
        notification.key === 'DELETE_TABLE_SCHEMA_COMPLETED_EVENT' ||
        notification.key === 'RESTORE_DATASET_SCHEMA_SNAPSHOT_COMPLETED_EVENT'
    );

    const dataflowId = notification.content.dataflowId;
    const datasetId = notification.content.datasetId;

    const {
      dataflow: { name: dataflowName },
      dataset: { name: datasetName }
    } = await MetadataUtils.getMetadata({ dataflowId, datasetId });

    notificationContext.add({
      type: 'VALIDATE_DATA_INIT',
      content: {
        countryName: 'DESIGN',
        dataflowId,
        dataflowName,
        datasetId,
        datasetName
      }
    });
  };

  useCheckNotifications(
    [
      'IMPORT_DESIGN_COMPLETED_EVENT',
      'EXTERNAL_IMPORT_DESIGN_COMPLETED_EVENT',
      'EXTERNAL_IMPORT_DESIGN_FROM_OTHER_SYSTEM_COMPLETED_EVENT',
      'DELETE_TABLE_SCHEMA_COMPLETED_EVENT',
      'RESTORE_DATASET_SCHEMA_SNAPSHOT_COMPLETED_EVENT'
    ],
    notifyValidateDataInitDesign
  );

  return <Fragment />;
};

export { GlobalNotifications };
