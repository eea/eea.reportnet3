import React, { useContext } from 'react';

import { Button } from 'ui/views/_components/Button';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const Footer = ({ hasWritePermissions, isDataflowOpen, isDesignDataflowEditorRead, onAddClick, onPasteClick }) => {
  const resources = useContext(ResourcesContext);
  return (
    <div className="p-clearfix datasetSchema-addRecordsBar-help-step" style={{ width: '100%' }}>
      <Button
        className={`p-button-secondary ${isDataflowOpen ? null : 'p-button-animated-blink'}`}
        icon="add"
        disabled={!hasWritePermissions || isDataflowOpen || isDesignDataflowEditorRead}
        label={resources.messages['addRecord']}
        onClick={() => onAddClick()}
        style={{ float: 'left' }}
      />
      <Button
        className={`p-button-secondary ${
          isDataflowOpen || isDesignDataflowEditorRead ? null : 'p-button-animated-blink'
        }`}
        disabled={isDataflowOpen || isDesignDataflowEditorRead}
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
