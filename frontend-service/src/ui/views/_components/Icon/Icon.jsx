import React from 'react';

import { config } from 'assets/conf';

export const Icon = ({ icon }) => {
  return <i className={config.icons[icon]} />;
};
