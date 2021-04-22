import { Fragment, useContext, useEffect } from 'react';

import { DownloadFile } from 'ui/views/_components/DownloadFile';

import { DatasetService } from 'core/services/Dataset';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';

import { useCheckNotifications } from 'ui/views/_functions/Hooks/useCheckNotifications';

import { MetadataUtils } from 'ui/views/_functions/Utils';

const GlobalNotifications = () => {
  const notificationContext = useContext(NotificationContext);

  useEffect(() => {
    if (findHiddenExportFMENotification()) downloadExportFMEFile();

    if (findHiddenExportDatasetNotification()) downloadExportDatasetFile();
  }, [notificationContext.hidden]);

  const findHiddenExportFMENotification = () => {
    return notificationContext.hidden.find(
      notification =>
        notification.key === 'EXTERNAL_EXPORT_DESIGN_COMPLETED_EVENT' || 'EXTERNAL_EXPORT_REPORTING_COMPLETED_EVENT'
    );
  };

  const findHiddenExportDatasetNotification = () => {
    return notificationContext.hidden.find(notification => notification.key === 'EXPORT_DATASET_COMPLETED_EVENT');
  };

  const downloadExportDatasetFile = async () => {
    try {
      const [notification] = notificationContext.hidden.filter(
        notification => notification.key === 'EXPORT_DATASET_COMPLETED_EVENT'
      );

      const getFileName = () => {
        const extension = notification.content.datasetName.split('.').pop();
        return `${notification.content.datasetName}.${extension}`;
      };

      let datasetData;

      if (notification) {
        const { data } = await DatasetService.downloadExportDatasetFile(
          notification.content.datasetId,
          notification.content.datasetName
        );
        datasetData = data;

        notificationContext.add({
          type: 'EXTERNAL_INTEGRATION_DOWNLOAD',
          onClick: () => DownloadFile(datasetData, getFileName())
        });
      }
    } catch (error) {
      console.error(error);
      notificationContext.add({ type: 'DOWNLOAD_EXPORT_DATASET_FILE_ERROR' });
    } finally {
      notificationContext.clearHiddenNotifications();
    }
  };

  const downloadExportFMEFile = async () => {
    try {
      const [notification] = notificationContext.hidden.filter(
        notification =>
          notification.key === 'EXTERNAL_EXPORT_DESIGN_COMPLETED_EVENT' ||
          notification.key === 'EXTERNAL_EXPORT_REPORTING_COMPLETED_EVENT'
      );

      const getFileName = () => {
        const extension = notification.content.fileName.split('.').pop();
        return `${notification.content.datasetName}.${extension}`;
      };

      let datasetData;

      if (notification) {
        if (notification.content.providerId) {
          const { data } = await DatasetService.downloadExportFile(
            notification.content.datasetId,
            notification.content.fileName,
            notification.content.providerId
          );
          datasetData = data;
        } else {
          const { data } = await DatasetService.downloadExportFile(
            notification.content.datasetId,
            notification.content.fileName
          );
          datasetData = data;
        }

        notificationContext.add({
          type: 'EXTERNAL_INTEGRATION_DOWNLOAD',
          onClick: () => DownloadFile(datasetData, getFileName())
        });
      }
    } catch (error) {
      console.error(error);
      notificationContext.add({ type: 'DOWNLOAD_FME_FILE_ERROR' });
    } finally {
      notificationContext.clearHiddenNotifications();
    }
  };

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
