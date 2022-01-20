import isNil from 'lodash/isNil';
import { Fragment, useContext, useState } from 'react';

import { Button } from 'views/_components/Button';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const DeleteDatasetDataDialog = ({ children, disabled = false, onConfirmDelete, onHideDelete = () => {} }) => {
  const resourcesContext = useContext(ResourcesContext);

  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);

  const onConfirm = () => {
    setIsDeleteDialogVisible(false);
    if (!isNil(onConfirmDelete)) {
      onConfirmDelete();
    }
  };

  const onHide = () => {
    setIsDeleteDialogVisible(false);
    if (!isNil(onHideDelete)) {
      onHideDelete();
    }
  };

  const renderValidateButton = () => {
    return (
      <Button
        className={`p-button-rounded p-button-secondary-transparent ${
          !disabled ? 'p-button-animated-blink' : ''
        } dataset-deleteDataset-help-step`}
        disabled={disabled}
        icon="trash"
        label={resourcesContext.messages['deleteDatasetData']}
        onClick={() => setIsDeleteDialogVisible(true)}
      />
    );
  };

  const renderValidateDialog = () => {
    if (isDeleteDialogVisible) {
      return (
        <ConfirmDialog
          classNameConfirm="p-button-danger"
          header={resourcesContext.messages['deleteDatasetHeader']}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={onConfirm}
          onHide={onHide}
          visible={isDeleteDialogVisible}>
          <div>{resourcesContext.messages['deleteDatasetConfirm']}</div>
          {children}
        </ConfirmDialog>
      );
    }
  };

  return (
    <Fragment>
      {renderValidateButton()}
      {renderValidateDialog()}
    </Fragment>
  );
};
