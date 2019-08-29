import React from 'react';

import { config } from 'conf';

export const Icon = ({ icon, style }) => {
  return <i className={config.icons[icon]} style={style} />;
};
