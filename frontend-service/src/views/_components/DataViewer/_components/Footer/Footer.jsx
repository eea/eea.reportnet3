import { useContext } from 'react';

import { Button } from 'views/_components/Button';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

const Footer = ({ hasWritePermissions, isDataflowOpen, isDesignDatasetEditorRead, onAddClick, onPasteClick }) => {
  const resourcesContext = useContext(ResourcesContext);
  return (
    <div className="p-clearfix datasetSchema-addRecordsBar-help-step" style={{ width: '100%' }}>
      <Button
        className={`p-button-secondary ${isDataflowOpen ? null : 'p-button-animated-blink'}`}
        disabled={!hasWritePermissions || isDataflowOpen || isDesignDatasetEditorRead}
        icon="add"
        label={resourcesContext.messages['addRecord']}
        onClick={() => onAddClick()}
        style={{ float: 'left' }}
      />
      <Button
        className={`p-button-secondary ${
          isDataflowOpen || isDesignDatasetEditorRead ? null : 'p-button-animated-blink'
        }`}
        disabled={isDataflowOpen || isDesignDatasetEditorRead}
        icon="clipboard"
        label={resourcesContext.messages['pasteRecords']}
        onClick={async () => {
          onPasteClick();
        }}
        style={{ float: 'right' }}
      />
    </div>
  );
};

export { Footer };
