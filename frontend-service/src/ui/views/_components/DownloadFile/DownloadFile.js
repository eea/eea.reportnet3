import React from 'react';
import isUndefined from 'lodash/isUndefined';

export const DownloadFile = (data, fileName) => {
  const url = window.URL.createObjectURL(new Blob([data]));

  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', fileName);

  document.body.appendChild(link);

  link.click();

  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
};
