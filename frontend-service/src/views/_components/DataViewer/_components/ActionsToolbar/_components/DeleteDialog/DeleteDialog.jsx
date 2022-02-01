import { Fragment, useContext, useState } from 'react';

import isNil from 'lodash/isNil';

import { Button } from 'views/_components/Button';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const DeleteDialog = ({ disabled, hasWritePermissions, onConfirmDeleteTable, showWriteButtons, tableName }) => {
  const resourcesContext = useContext(ResourcesContext);

  const [isDialogVisible, setIsDialogVisible] = useState(false);

  const onConfirm = () => {
    setIsDialogVisible(false);
    if (!isNil(onConfirmDeleteTable)) {
      onConfirmDeleteTable();
    }
  };

  const renderButton = () => {
    if (hasWritePermissions || showWriteButtons) {
      return (
        <Button
          className={`p-button-rounded p-button-secondary-transparent datasetSchema-delete-table-help-step ${
            !disabled && 'p-button-animated-blink'
          }`}
          disabled={disabled}
          icon="trash"
          label={resourcesContext.messages['deleteTable']}
          onClick={() => setIsDialogVisible(true)}
        />
      );
    }
  };

  const renderDialog = () => {
    if (isDialogVisible) {
      return (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={`${resourcesContext.messages['deleteDatasetTableHeader']} (${tableName})`}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={onConfirm}
          onHide={() => setIsDialogVisible(false)}
          visible={isDialogVisible}>
          {resourcesContext.messages['deleteDatasetTableConfirm']}
        </ConfirmDialog>
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
