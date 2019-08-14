import React from 'react';

import { config } from 'conf';

export const Icon = ({ icon }) => {
  return <i className={config.icons[icon]} />;
};
