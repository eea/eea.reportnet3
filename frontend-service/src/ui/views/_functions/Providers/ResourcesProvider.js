import React, { useState } from 'react';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import langResources from 'conf/messages.en.json';

export const ResourcesProvider = ({ children }) => {
  const [resources] = useState({ ...langResources });

  return <ResourcesContext.Provider value={resources}>{children}</ResourcesContext.Provider>;
};
