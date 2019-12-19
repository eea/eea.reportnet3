import React, { useContext } from 'react';

import { Button } from 'ui/views/_components/Button';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const Footer = ({ hasWritePermissions, onAddClick, onPasteClick }) => {
  const resources = useContext(ResourcesContext);
  return (
    <div className="p-clearfix" style={{ width: '100%' }}>
      <Button
        style={{ float: 'left' }}
        label={resources.messages['add']}
        icon="add"
        disabled={!hasWritePermissions}
        onClick={() => onAddClick()}
      />
      <Button
        style={{ float: 'right' }}
        label={resources.messages['pasteRecords']}
        icon="clipboard"
        onClick={async () => {
          onPasteClick();
        }}
      />
    </div>
  );
};

export { Footer };
