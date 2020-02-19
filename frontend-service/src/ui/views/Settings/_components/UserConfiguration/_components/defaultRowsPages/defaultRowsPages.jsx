import React, { useContext } from 'react';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const DefaultRowsPages = () => {
  const resources = useContext(ResourcesContext);

  return (
    <h3>{resources.messages['defaultRowsPage']}</h3>,
    
      <Dropdown
        name="dataProvidersDropdown"
        optionLabel="label"
        placeholder={resources.messages['manageRolesDialogDropdownPlaceholder']}
        // value={formState.selectedDataProviderGroup}
        options={[1, 3, 5]}
        onChange={e => {}}
      />
    
  );
};
