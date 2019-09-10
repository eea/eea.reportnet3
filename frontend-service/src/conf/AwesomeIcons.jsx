import React from 'react';

import { faFilePdf, faFileExcel, faFileCsv, faFileWord, faFileAlt } from '@fortawesome/free-solid-svg-icons';

export const AwesomeIcons = icon => {
  switch (icon) {
    case 'pdf':
      return faFilePdf;
    case 'xls':
      return faFileExcel;
    case 'xlsx':
      return faFileExcel;
    case 'csv':
      return faFileCsv;
    case 'doc':
      return faFileWord;
    case 'docx':
      return faFileWord;
    default:
      return faFileAlt;
  }
};
