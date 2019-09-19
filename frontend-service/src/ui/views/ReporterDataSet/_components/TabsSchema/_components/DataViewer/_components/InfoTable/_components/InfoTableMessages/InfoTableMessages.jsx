import React, { useContext } from 'react';

import { isUndefined } from 'lodash';

import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

export const InfoTableMessages = ({ data, columns, records }) => {
  const resources = useContext(ResourcesContext);

  const checkPastedColumnsErrors = () => {
    //Data columns minus "actions", "recordValidations", "dataSetPartitionId" and "id" columnns
    const filteredColumns = columns.filter(
      column =>
        column.key !== 'actions' &&
        column.key !== 'recordValidation' &&
        column.key !== 'id' &&
        column.key !== 'dataSetPartitionId'
    );

    const numCopiedCols = data.map(rows => rows.copiedCols);
    const equalNumberColumns = numCopiedCols.filter(colNum => {
      return colNum !== filteredColumns.length;
    });

    if (!isUndefined(data)) {
      if (data.length > 0) {
        if (equalNumberColumns.length > 0) {
          return (
            <div>
              <p style={{ fontWeight: 'bold', color: '#DA2131' }}>{resources.messages['pasteColumnWarningMessage']}</p>
              {records > 500 ? (
                <p style={{ fontWeight: 'bold', color: '#DA2131' }}>
                  {resources.messages['pasteRecordsWarningMessage']}
                </p>
              ) : null}
              <p>{resources.messages['pasteColumnWarningConfirmMessage']}</p>
            </div>
          );
        } else {
          return (
            <div>
              <p>{resources.messages['pasteColumnConfirmMessage']}</p>
            </div>
          );
        }
      }
    }
  };

  return (
    <React.Fragment>
      <p>
        {resources.messages['pastedRecordsMessage']} {!isUndefined(data) ? data.length : 0}
      </p>
      {!isUndefined(data) ? checkPastedColumnsErrors() : null}
    </React.Fragment>
  );
};
