import { useContext } from 'react';

import isNil from 'lodash/isNil';

import styles from './ActionsColumn.module.scss';

import { Button } from 'views/_components/Button';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const ActionsColumn = ({
  bigData,
  isIcebergCreated,
  disabledButtons,
  hideDeletion = false,
  hideEdition = false,
  isDeletingDocument,
  isDeletingDatasetData,
  isUpdating,
  isWebform = false,
  onCloneClick,
  onDeleteClick,
  onEditClick,
  onViewClick,
  rowDataId,
  rowDeletingId,
  rowUpdatingId,
  tooltip
}) => {
  const resourcesContext = useContext(ResourcesContext);
  return (
    <div className={styles.actionTemplate}>
      {!isNil(onViewClick) && !hideEdition && !isIcebergCreated && (
        <Button
          className={`${`p-button-rounded p-button-secondary-transparent ${styles.editRowButton}`} ${
            (rowDeletingId === rowDataId && isDeletingDocument) ||
            (rowUpdatingId === rowDataId && isUpdating) ||
            disabledButtons
              ? null
              : 'p-button-animated-blink'
          }`}
          icon={rowUpdatingId !== rowDataId || !isUpdating ? 'eye' : 'spinnerAnimate'}
          onClick={onViewClick}
          tooltip={resourcesContext.messages['view']}
          tooltipOptions={{ position: 'top' }}
          type="button"
        />
      )}
      {!isNil(onEditClick) && !hideEdition && (
        <Button
          className={`${`p-button-rounded p-button-secondary-transparent ${styles.editRowButton}`} ${
            (rowDeletingId === rowDataId && isDeletingDocument) ||
            (rowUpdatingId === rowDataId && isUpdating) ||
            disabledButtons
              ? null
              : 'p-button-animated-blink'
          }`}
          disabled={
            (isWebform && bigData) ||
            (rowDeletingId === rowDataId && isDeletingDocument) ||
            (rowUpdatingId === rowDataId && isUpdating) ||
            disabledButtons
          }
          icon={rowUpdatingId !== rowDataId || !isUpdating ? 'edit' : 'spinnerAnimate'}
          onClick={onEditClick}
          tooltip={resourcesContext.messages['edit']}
          tooltipOptions={{ position: 'top' }}
          type="button"
        />
      )}
      {!isNil(onCloneClick) && !hideDeletion && (
        <Button
          className={`${`p-button-rounded p-button-secondary-transparent ${styles.editRowButton}`} ${
            (rowDeletingId === rowDataId && isDeletingDocument) ||
            (rowUpdatingId === rowDataId && isUpdating) ||
            disabledButtons
              ? null
              : 'p-button-animated-blink'
          }`}
          disabled={
            (rowDeletingId === rowDataId && isDeletingDocument) ||
            (rowUpdatingId === rowDataId && isUpdating) ||
            disabledButtons
          }
          icon="clone"
          onClick={onCloneClick}
          tooltip={resourcesContext.messages['duplicate']}
          tooltipOptions={{ position: 'top' }}
          type="button"
        />
      )}
      {!isNil(onDeleteClick) && !hideDeletion && (
        <Button
          className={`${`p-button-rounded p-button-secondary-transparent ${styles.deleteRowButton}`} ${
            isDeletingDocument ||
            isDeletingDatasetData ||
            (rowUpdatingId === rowDataId && isUpdating) ||
            disabledButtons
              ? null
              : 'p-button-animated-blink'
          }`}
          disabled={
            (isWebform && bigData && !isIcebergCreated) ||
            isDeletingDocument ||
            isDeletingDatasetData ||
            (rowUpdatingId === rowDataId && isUpdating) ||
            disabledButtons
          }
          icon={
            rowDeletingId !== rowDataId || !isDeletingDatasetData || !isDeletingDocument ? 'trash' : 'spinnerAnimate'
          }
          onClick={onDeleteClick}
          tooltip={tooltip ?? resourcesContext.messages['delete']}
          tooltipOptions={{ position: 'top' }}
          type="button"
        />
      )}
    </div>
  );
};
