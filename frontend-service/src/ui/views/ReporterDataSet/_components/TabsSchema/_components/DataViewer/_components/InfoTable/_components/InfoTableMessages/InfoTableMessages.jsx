import React, { useContext } from 'react';

import { isUndefined } from 'lodash';

import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

export const InfoTableMessages = ({ data, filteredColumns, numRecords }) => {
  const resources = useContext(ResourcesContext);

  const checkPastedColumnsErrors = () => {
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
              {numRecords > 500 ? (
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

  return <React.Fragment>{!isUndefined(data) ? checkPastedColumnsErrors() : null}</React.Fragment>;
};
