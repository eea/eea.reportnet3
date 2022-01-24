import { Fragment, useContext, useState } from 'react';

import { Button } from 'views/_components/Button';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const DatasetValidateDialog = ({ disabled, onConfirmValidate }) => {
  const resourcesContext = useContext(ResourcesContext);

  const [isValidateDialogVisible, setIsValidateDialogVisible] = useState(false);

  const onConfirm = () => {
    setIsValidateDialogVisible(false);
    onConfirmValidate();
  };

  const renderValidateButton = () => {
    return (
      <Button
        className={`p-button-rounded p-button-secondary-transparent ${!disabled ? ' p-button-animated-blink' : null}`}
        disabled={disabled}
        icon="validate"
        label={resourcesContext.messages['validate']}
        onClick={() => setIsValidateDialogVisible(true)}
      />
    );
  };

  const renderValidateDialog = () => {
    if (isValidateDialogVisible) {
      return (
        <ConfirmDialog
          header={resourcesContext.messages['validateDataset']}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={onConfirm}
          onHide={() => setIsValidateDialogVisible(false)}
          visible={isValidateDialogVisible}>
          {resourcesContext.messages['validateDatasetConfirm']}
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
