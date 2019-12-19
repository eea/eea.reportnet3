import React, { useContext } from 'react';

import { isUndefined } from 'lodash';

import colors from 'conf/colors.json';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const InfoTableMessages = ({ data, filteredColumns, numCopiedRecords }) => {
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
              <p style={{ fontWeight: 'bold', color: colors.errors }}>
                {resources.messages['pasteColumnWarningMessage']}
              </p>
              {numCopiedRecords > 500 ? (
                <p style={{ fontWeight: 'bold', color: colors.errors }}>
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
