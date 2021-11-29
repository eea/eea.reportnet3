import { useContext, useEffect } from 'react';

import isNil from 'lodash/isNil';

import { DownloadFile } from 'views/_components/DownloadFile';

import { DataflowService } from 'services/DataflowService';
import { DatasetService } from 'services/DatasetService';
import { ValidationService } from 'services/ValidationService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';

import { useCheckNotifications } from 'views/_functions/Hooks/useCheckNotifications';

import { MetadataUtils } from 'views/_functions/Utils';

const GlobalNotifications = () => {
  const notificationContext = useContext(NotificationContext);

  useEffect(() => {
    downloadAllSchemasInfoFile();
    downloadQCRulesFile();
    downloadUsersListFile();
    downloadValidationsFile();
    downloadExportFMEFile();
    downloadExportDatasetFile();
    getErrorExportDatasetNotification();
    clearSystemNotifications();
  }, [notificationContext.hidden]);

  const clearSystemNotifications = () => {
    const notification = findHiddenNotification('NO_ENABLED_SYSTEM_NOTIFICATIONS');

    if (isNil(notification)) {
      return;
    }

    notificationContext.deleteAll(true);
  };

  const findHiddenNotification = key => notificationContext.hidden.find(notification => notification.key === key);

  const downloadAllSchemasInfoFile = async () => {
    const notification = findHiddenNotification('EXPORT_SCHEMA_INFORMATION_COMPLETED_EVENT');

    if (isNil(notification)) {
      return;
    }

    try {
      const { data } = await DataflowService.downloadAllSchemasInfo(
        notification.content.dataflowId,
        notification.content.fileName
      );
      notificationContext.add({ type: 'AUTOMATICALLY_DOWNLOAD_SCHEMAS_INFO_FILE' });

      if (data.size !== 0) {
        DownloadFile(data, notification.content.fileName);
      }
    } catch (error) {
      console.error('GlobalNotifications - downloadAllSchemasInfoFile.', error);
      if (error.response?.status === 400) {
        notificationContext.add({ type: 'DOWNLOAD_FILE_BAD_REQUEST_ERROR' }, true);
      } else {
        notificationContext.add({ type: 'DOWNLOAD_SCHEMAS_INFO_FILE_ERROR' }, true);
      }
    } finally {
      notificationContext.clearHiddenNotifications();
    }
  };

  const downloadQCRulesFile = async () => {
    const notification = findHiddenNotification('EXPORT_QC_COMPLETED_EVENT');

    if (isNil(notification)) {
      return;
    }

    try {
      const { data } = await ValidationService.downloadQCRulesFile(
        notification.content.datasetId,
        notification.content.fileName
      );
      notificationContext.add({ type: 'AUTOMATICALLY_DOWNLOAD_QC_RULES_FILE' });

      if (data.size !== 0) {
        DownloadFile(data, notification.content.fileName);
      }
    } catch (error) {
      console.error('GlobalNotifications - downloadQCRulesFile.', error);
      if (error.response?.status === 400) {
        notificationContext.add({ type: 'DOWNLOAD_FILE_BAD_REQUEST_ERROR' }, true);
      } else {
        notificationContext.add({ type: 'DOWNLOAD_QC_RULES_FILE_ERROR' }, true);
      }
    } finally {
      notificationContext.clearHiddenNotifications();
    }
  };

  const downloadUsersListFile = async () => {
    const notification = findHiddenNotification('EXPORT_USERS_BY_COUNTRY_COMPLETED_EVENT');

    if (isNil(notification)) {
      return;
    }

    try {
      const { data } = await DataflowService.downloadUsersListFile(
        notification.content.dataflowId,
        notification.content.nameFile
      );
      notificationContext.add({ type: 'AUTOMATICALLY_DOWNLOAD_USERS_LIST_FILE' });

      if (data.size !== 0) {
        DownloadFile(data, notification.content.nameFile);
      }
    } catch (error) {
      console.error('GlobalNotifications - downloadUsersListFile.', error);
      if (error.response?.status === 400) {
        notificationContext.add({ type: 'DOWNLOAD_FILE_BAD_REQUEST_ERROR' }, true);
      } else {
        notificationContext.add({ type: 'DOWNLOAD_USERS_LIST_FILE_ERROR' }, true);
      }
    } finally {
      notificationContext.clearHiddenNotifications();
    }
  };

  const downloadValidationsFile = async () => {
    const notification = findHiddenNotification('DOWNLOAD_VALIDATIONS_COMPLETED_EVENT');

    if (isNil(notification)) {
      return;
    }

    try {
      const { data } = await ValidationService.downloadShowValidationsFile(
        notification.content.datasetId,
        notification.content.nameFile
      );
      notificationContext.add({ type: 'AUTOMATICALLY_DOWNLOAD_VALIDATIONS_FILE' });

      if (data.size !== 0) {
        DownloadFile(data, notification.content.nameFile);
      }
    } catch (error) {
      console.error('GlobalNotifications - downloadValidationsFile.', error);
      if (error.response?.status === 400) {
        notificationContext.add({ type: 'DOWNLOAD_FILE_BAD_REQUEST_ERROR' }, true);
      } else {
        notificationContext.add({ type: 'DOWNLOAD_VALIDATIONS_FILE_ERROR' }, true);
      }
    } finally {
      notificationContext.clearHiddenNotifications();
    }
  };
  const downloadExportFMEFile = async () => {
    const notification = notificationContext.hidden.find(
      notification =>
        notification.key === 'EXTERNAL_EXPORT_DESIGN_COMPLETED_EVENT' ||
        notification.key === 'EXTERNAL_EXPORT_REPORTING_COMPLETED_EVENT'
    );

    if (isNil(notification)) {
      return;
    }

    try {
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
          type: 'EXPORT_DATASET_FILE_DOWNLOAD',
          onClick: () => datasetData.size !== 0 && DownloadFile(datasetData, getFileName())
        });
      }
    } catch (error) {
      console.error('GlobalNotifications - downloadExportFMEFile.', error);
      notificationContext.add({ type: 'DOWNLOAD_FME_FILE_ERROR' }, true);
    } finally {
      notificationContext.clearHiddenNotifications();
    }
  };

  const downloadExportDatasetFile = async () => {
    const notification = findHiddenNotification('EXPORT_DATASET_COMPLETED_EVENT');

    if (isNil(notification)) {
      return;
    }

    try {
      notificationContext.add({ type: 'EXPORT_DATASET_FILE_AUTOMATICALLY_DOWNLOAD' });

      const { data } = await DatasetService.downloadExportDatasetFile(
        notification.content.datasetId,
        notification.content.datasetName
      );

      if (data.size !== 0) {
        DownloadFile(data, notification.content.datasetName);
      }
    } catch (error) {
      console.error('GlobalNotifications - downloadExportDatasetFile.', error);
      notificationContext.add({ type: 'DOWNLOAD_EXPORT_DATASET_FILE_ERROR' }, true);
    } finally {
      notificationContext.clearHiddenNotifications();
    }
  };

  const getErrorExportDatasetNotification = () => {
    const notification = findHiddenNotification('EXPORT_DATASET_FAILED_EVENT');

    if (isNil(notification)) {
      return;
    }

    const dataflowId = notification.content.dataflowId;
    const datasetId = notification.content.datasetId;
    const datasetName = notification.content.datasetName;

    if (notification.content.datasetType === 'REPORTING' || notification.content.datasetType === 'TEST') {
      getNotificationByDatasetType(dataflowId, datasetId, datasetName, 'EXPORT_REPORTING_TEST_DATASET_ERROR');
    } else if (notification.content.datasetType === 'DESIGN') {
      getNotificationByDatasetType(dataflowId, datasetId, datasetName, 'EXPORT_DESIGNER_DATASET_ERROR');
    } else if (notification.content.datasetType === 'COLLECTION') {
      getNotificationByDatasetType(dataflowId, datasetId, datasetName, 'EXPORT_DATA_COLLECTION_DATASET_ERROR');
    } else {
      getNotificationByDatasetType(dataflowId, datasetId, datasetName, 'EXPORT_EU_DATASET_DATASET_ERROR');
    }
  };

  const getNotificationByDatasetType = (dataflowId, datasetId, datasetName, datasetType) => {
    return notificationContext.add({
      type: datasetType,
      content: {
        dataflowId,
        datasetId,
        datasetName
      }
    });
  };

  const notifyValidateDataInitDesign = async () => {
    const notification = notificationContext.toShow.find(
      notification =>
        notification.key === 'IMPORT_DESIGN_COMPLETED_EVENT' ||
        notification.key === 'EXTERNAL_IMPORT_DESIGN_COMPLETED_EVENT' ||
        notification.key === 'EXTERNAL_IMPORT_DESIGN_FROM_OTHER_SYSTEM_COMPLETED_EVENT' ||
        notification.key === 'DELETE_TABLE_SCHEMA_COMPLETED_EVENT' ||
        notification.key === 'DELETE_DATASET_SCHEMA_COMPLETED_EVENT' ||
        notification.key === 'RESTORE_DATASET_SCHEMA_SNAPSHOT_COMPLETED_EVENT'
    );

    const dataflowId = notification.content.dataflowId;
    const datasetId = notification.content.datasetId;

    const {
      dataflow: { name: dataflowName },
      dataset: { name: datasetName }
    } = await MetadataUtils.getMetadata({ dataflowId, datasetId });

    notificationContext.add(
      {
        type: 'VALIDATE_DATA_INIT',
        content: {
          customContent: { origin: 'DESIGN' },
          dataflowId,
          dataflowName,
          datasetId,
          datasetName,
          type: 'DESIGN'
        }
      },
      true
    );
  };

  useCheckNotifications(
    [
      'IMPORT_DESIGN_COMPLETED_EVENT',
      'EXTERNAL_IMPORT_DESIGN_COMPLETED_EVENT',
      'EXTERNAL_IMPORT_DESIGN_FROM_OTHER_SYSTEM_COMPLETED_EVENT',
      'DELETE_TABLE_SCHEMA_COMPLETED_EVENT',
      'DELETE_DATASET_SCHEMA_COMPLETED_EVENT',
      'RESTORE_DATASET_SCHEMA_SNAPSHOT_COMPLETED_EVENT'
    ],
    notifyValidateDataInitDesign
  );

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

    notificationContext.add(
      {
        type: 'VALIDATE_DATA_INIT',
        content: {
          customContent: { origin: 'REPORTING' },
          dataflowId,
          dataflowName,
          datasetId,
          datasetName,
          type: 'REPORTING'
        }
      },
      true
    );
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

  return <div />;
};

export { GlobalNotifications };
