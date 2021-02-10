import React from 'react';

import isNil from 'lodash/isNil';

import styles from './ActionsColumn.module.css';

import { Button } from 'ui/views/_components/Button';

const ActionsColumn = ({
  disabledButtons,
  hideDeletion = false,
  hideEdition = false,
  isDataflowOpen,
  isDeletingDocument,
  isUpdating,
  onDeleteClick,
  onEditClick,
  rowDataId,
  rowDeletingId,
  rowUpdatingId
}) => {
  return (
    <div className={styles.actionTemplate}>
      {!isNil(onEditClick) && !hideEdition && (
        <Button
          className={`${`p-button-rounded p-button-secondary-transparent ${styles.editRowButton}`} ${
            isDataflowOpen ? null : 'p-button-animated-blink'
          }`}
          disabled={
            (rowDeletingId === rowDataId && isDeletingDocument) ||
            (rowUpdatingId === rowDataId && isUpdating) ||
            disabledButtons ||
            isDataflowOpen
          }
          icon={rowUpdatingId !== rowDataId || !isUpdating ? 'edit' : 'spinnerAnimate'}
          onClick={() => onEditClick()}
          type="button"
        />
      )}
      {!isNil(onDeleteClick) && !hideDeletion && (
        <Button
          className={`${`p-button-rounded p-button-secondary-transparent ${styles.deleteRowButton}`} ${
            isDataflowOpen ? null : 'p-button-animated-blink'
          }`}
          disabled={
            isDeletingDocument || (rowUpdatingId === rowDataId && isUpdating) || disabledButtons || isDataflowOpen
          }
          icon={rowDeletingId !== rowDataId || !isDeletingDocument ? 'trash' : 'spinnerAnimate'}
          onClick={() => onDeleteClick()}
          type="button"
        />
      )}
    </div>
  );
};

export { ActionsColumn };
