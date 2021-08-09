import { useState } from 'react';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import langResources from 'conf/messages.en.json';
import userPresets from 'conf/userPresets.json';
export const ResourcesProvider = ({ children }) => {
  const [resources] = useState({ ...langResources, ...userPresets });

  return <ResourcesContext.Provider value={resources}>{children}</ResourcesContext.Provider>;
};
