import { useContext } from 'react';

import { Button } from 'views/_components/Button';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const Footer = ({
  bigData,
  dataAreManuallyEditable,
  hasWritePermissions,
  isDataflowOpen,
  isDesignDatasetEditorRead,
  isEditRecordsManuallyButtonDisabled,
  isTableEditable,
  onAddClick,
  onPasteClick
}) => {
  const resourcesContext = useContext(ResourcesContext);
  return (
    <div className="p-clearfix datasetSchema-addRecordsBar-help-step" style={{ width: '100%' }}>
      {(!bigData || (bigData && isTableEditable && dataAreManuallyEditable)) && (
        <Button
          className={`${isDataflowOpen ? '' : 'p-button-animated-blink'}`}
          disabled={
            isEditRecordsManuallyButtonDisabled || !hasWritePermissions || isDataflowOpen || isDesignDatasetEditorRead
          }
          icon="add"
          label={resourcesContext.messages['addRecord']}
          onClick={onAddClick}
          style={{ float: 'left' }}
        />
      )}
      {(!bigData || (bigData && isTableEditable && dataAreManuallyEditable)) && (
        <Button
          className={`p-button-secondary ${
            isDataflowOpen || isDesignDatasetEditorRead ? null : 'p-button-animated-blink'
          }`}
          disabled={
            isEditRecordsManuallyButtonDisabled || !hasWritePermissions || isDataflowOpen || isDesignDatasetEditorRead
          }
          icon="clipboard"
          label={resourcesContext.messages['pasteRecords']}
          onClick={async () => {
            onPasteClick();
          }}
          style={{ float: 'right' }}
        />
      )}
    </div>
  );
};
