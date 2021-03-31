import React, { useContext } from 'react';

import { Button } from 'ui/views/_components/Button';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const Footer = ({ hasWritePermissions, isDataflowOpen, isDesignDatasetEditorRead, onAddClick, onPasteClick }) => {
  const resources = useContext(ResourcesContext);
  return (
    <div className="p-clearfix datasetSchema-addRecordsBar-help-step" style={{ width: '100%' }}>
      <Button
        className={`p-button-secondary ${isDataflowOpen ? null : 'p-button-animated-blink'}`}
        icon="add"
        disabled={!hasWritePermissions || isDataflowOpen || isDesignDatasetEditorRead}
        label={resources.messages['addRecord']}
        onClick={() => onAddClick()}
        style={{ float: 'left' }}
      />
      <Button
        className={`p-button-secondary ${
          isDataflowOpen || isDesignDatasetEditorRead ? null : 'p-button-animated-blink'
        }`}
        disabled={isDataflowOpen || isDesignDatasetEditorRead}
        icon="clipboard"
        label={resources.messages['pasteRecords']}
        onClick={async () => {
          onPasteClick();
        }}
        style={{ float: 'right' }}
      />
    </div>
  );
};

export { Footer };
