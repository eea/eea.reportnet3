import { Fragment, useContext, useState } from 'react';

import { config } from 'conf';

import { Button } from 'views/_components/Button';
import { CustomFileUpload } from 'views/_components/CustomFileUpload';
import { DatasetConfig } from 'repositories/config/DatasetConfig';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { getUrl } from 'repositories/_utils/UrlUtils';
import { MetadataUtils } from 'views/_functions/Utils';
import { TextUtils } from 'repositories/_utils/TextUtils';

export const ImportTableDataDialog = ({
  colsSchema,
  dataflowId,
  datasetId,
  hasWritePermissions,
  isDataflowOpen,
  isDesignDatasetEditorRead,
  showWriteButtons,
  tableId,
  tableName
}) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [importTableDialogVisible, setImportTableDialogVisible] = useState(false);

  const readLines = async function* (blob, encoding = 'utf-8', delimiter = /\r?\n/g) {
    const reader = blob.stream().getReader();
    const decoder = new TextDecoder(encoding);

    try {
      let text = '';

      while (true) {
        const { value, done } = await reader.read();
        if (done) break;
        text += decoder.decode(value, { stream: true });
        const lines = text.split(delimiter);
        text = lines.pop();
        yield* lines;
      }

      yield text;
    } finally {
      reader.cancel();
    }
  };

  const onImportTableError = async ({ xhr }) => {
    if (xhr.status === 423) {
      notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' }, true);
    }
  };

  const onUpload = async () => {
    setImportTableDialogVisible(false);
    const {
      dataflow: { name: dataflowName },
      dataset: { name: datasetName }
    } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
    notificationContext.add(
      {
        type: 'DATASET_DATA_LOADING_INIT',
        content: {
          dataflowName,
          datasetName,
          customContent: {
            datasetLoadingMessage: resourcesContext.messages['datasetLoadingMessage'],
            title: TextUtils.ellipsis(tableName, config.notifications.STRING_LENGTH_MAX),
            datasetLoading: resourcesContext.messages['datasetLoading']
          }
        }
      },
      true
    );
  };

  const onValidateFile = async file => {
    const checkFirstLine = async firstLine => {
      const validations = [];
      if (colsSchema?.length - 2 !== firstLine.split(',').length) {
        validations.push({
          severity: 'warn',
          summary: resourcesContext.messages['importWrongFileHeader'],
          detail: `${resourcesContext.messages['importWrongFileHeaderDetail']} ${
            resourcesContext.messages['columnsSchemaLabel']
          }: ${colsSchema?.length - 2} - ${resourcesContext.messages['fileColumnsLabel']}: ${
            firstLine.split(',').length
          }`
        });
      }
      return validations;
    };

    for await (const line of readLines(file, 'utf-8', '\n')) {
      return checkFirstLine(line);
    }
  };

  const renderButton = () => {
    if (hasWritePermissions || showWriteButtons) {
      return (
        <Button
          className={`p-button-rounded p-button-secondary datasetSchema-import-table-help-step ${
            !hasWritePermissions || isDataflowOpen || isDesignDatasetEditorRead ? null : 'p-button-animated-blink'
          }`}
          disabled={!hasWritePermissions || isDataflowOpen || isDesignDatasetEditorRead}
          icon="import"
          label={resourcesContext.messages['importTable']}
          onClick={() => setImportTableDialogVisible(true)}
        />
      );
    }
  };

  const renderDialog = () => {
    if (importTableDialogVisible) {
      return (
        <CustomFileUpload
          accept=".csv"
          chooseLabel={resourcesContext.messages['selectFile']}
          dialogHeader={`${resourcesContext.messages['uploadTable']}${tableName}`}
          dialogOnHide={() => setImportTableDialogVisible(false)}
          dialogVisible={importTableDialogVisible}
          infoTooltip={`${resourcesContext.messages['supportedFileExtensionsTooltip']} .csv`}
          invalidExtensionMessage={resourcesContext.messages['invalidExtensionFile']}
          isDialog={true}
          name="file"
          onError={onImportTableError}
          onUpload={onUpload}
          onValidateFile={onValidateFile}
          replaceCheck={true}
          url={`${window.env.REACT_APP_BACKEND}${getUrl(DatasetConfig.importFileTable, {
            datasetId: datasetId,
            tableSchemaId: tableId,
            delimiter: encodeURIComponent(config.IMPORT_FILE_DELIMITER)
          })}`}
        />
      );
    }
  };

  return (
    <Fragment>
      {renderButton()}
      {renderDialog()}
    </Fragment>
  );
};
