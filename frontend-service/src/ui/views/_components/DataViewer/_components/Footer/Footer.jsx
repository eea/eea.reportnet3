import React, { useContext } from 'react';

import { Button } from 'ui/views/_components/Button';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const Footer = ({ hasWritePermissions, onAddClick, onPasteClick }) => {
  const resources = useContext(ResourcesContext);
  return (
    <div className="p-clearfix" style={{ width: '100%' }}>
      <Button
        className="p-button-animated-blink"
        icon="add"
        disabled={!hasWritePermissions}
        label={resources.messages['addRecord']}
        onClick={() => onAddClick()}
        style={{ float: 'left' }}
      />
      <Button
        className="p-button-animated-blink"
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
