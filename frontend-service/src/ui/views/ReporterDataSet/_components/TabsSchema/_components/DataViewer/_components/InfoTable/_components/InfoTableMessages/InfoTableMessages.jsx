import React from 'react';

import { isUndefined } from 'lodash';

export const InfoTableMessages = ({ data, columns }) => {
  const checkPastedColumnsErrors = () => {
    let correctColumns = true;
    if (data) {
      if (data.length > 0) {
        let i = 0;
        //Data columns minus "actions", "recordValidations", "dataSetPartitionId" and "id" columnns
        const filteredColumns = columns.filter(
          column =>
            column.key !== 'actions' &&
            column.key !== 'recordValidation' &&
            column.key !== 'id' &&
            column.key !== 'dataSetPartitionId'
        );
        do {
          if (!isUndefined(data[i])) {
            const nonEmptyColumns = data[i].dataRow.filter(d => !isUndefined(Object.values(d.fieldData)[0]));
            if (nonEmptyColumns.length !== filteredColumns.length) {
              correctColumns = false;
            }
            i++;
          }
        } while (i < data.length && correctColumns);

        if (!correctColumns) {
          return (
            <div>
              <p style={{ fontWeight: 'bold', color: '#DA2131' }}>
                Warning! There are rows with a wrong number of columns pasted
              </p>
              <p>Do you still want to paste this data?</p>
            </div>
          );
        } else {
          return (
            <div>
              <p>Do you want to paste this data?</p>
            </div>
          );
        }
      }
    }
  };

  return (
    <React.Fragment>
      <p>Pasted records: {!isUndefined(data) ? data.length : 0}</p>
      {!isUndefined(data) ? checkPastedColumnsErrors() : null}
    </React.Fragment>
  );
};
