import { Fragment, useContext } from 'react';

import isUndefined from 'lodash/isUndefined';

import colors from 'conf/colors.json';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const InfoTableMessages = ({ checkValidCoordinates, data, filteredColumns, numCopiedRecords }) => {
  const resources = useContext(ResourcesContext);

  const checkPastedColumnsErrors = () => {
    const numCopiedCols = data.map(rows => rows.copiedCols);
    const equalNumberColumns = numCopiedCols.filter(colNum => {
      return colNum !== filteredColumns.length;
    });

    if (!isUndefined(data)) {
      if (data.length > 0) {
        if (equalNumberColumns.length > 0 || !checkValidCoordinates()) {
          return (
            <div>
              {equalNumberColumns.length > 0 && (
                <p style={{ fontWeight: 'bold', color: colors.errors }}>
                  {resources.messages['pasteColumnWarningMessage']}
                </p>
              )}
              {numCopiedRecords > 500 ? (
                <p style={{ fontWeight: 'bold', color: colors.errors }}>
                  {resources.messages['pasteRecordsWarningMessage']}
                </p>
              ) : null}
              {!checkValidCoordinates() && (
                <Fragment>
                  <p style={{ fontWeight: 'bold', color: colors.errors }}>
                    {resources.messages['pasteRecordsWarningCoordinatesMessage']}
                  </p>
                  <p style={{ fontStyle: 'italic', fontSize: '0.9em', fontWeight: 'bold' }}>
                    {`${resources.messages['pasteRecordsCoordinatesMessage']}(${resources.messages['pasteRecordsCoordinatesStructureMessage']})`}
                  </p>
                </Fragment>
              )}
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

  // eslint-disable-next-line react/jsx-no-useless-fragment
  return <Fragment>{!isUndefined(data) && checkPastedColumnsErrors()}</Fragment>;
};
