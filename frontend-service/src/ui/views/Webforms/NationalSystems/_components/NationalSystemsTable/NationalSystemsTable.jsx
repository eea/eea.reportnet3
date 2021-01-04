import React, { useEffect, useReducer } from 'react';

import { DatasetService } from 'core/services/Dataset';

export const NationalSystemsTable = ({ data, datasetId, tableSchemaId }) => {
  useEffect(() => {
    onLoadTableData();
  }, []);

  const onLoadTableData = async () => {
    try {
      const response = await DatasetService.tableDataById(datasetId, tableSchemaId, '', 100, undefined, [
        'CORRECT',
        'INFO',
        'WARNING',
        'ERROR',
        'BLOCKER'
      ]);

      // console.log('response', response);
    } catch (error) {
      console.log('error', error);
    }
  };

  const dataD = [
    { title: 'bla', tooltip: 'tool', data: 'aqui', attachment: '' },
    { title: 'bla2', tooltip: 'tool2', data: 'aqui2', attachment: '' },
    { title: 'bla3', tooltip: 'tool3', data: 'aqui3', attachment: '' }
  ];

  return dataD.map((dat, index) => (
    <div style={{ margin: '1rem' }}>
      TABLE: {index}
      <div> title: {dat.title}</div>
      <div> tooltip: {dat.tooltip}</div>
      <div> data: {dat.data}</div>
    </div>
  ));
};
