import { useContext } from 'react';

import isNil from 'lodash/isNil';

import styles from './ActionsColumn.module.scss';

import { Button } from 'views/_components/Button';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

const ActionsColumn = ({
  disabledButtons,
  hideDeletion = false,
  hideEdition = false,
  isDeletingDocument,
  isUpdating,
  onCloneClick,
  onDeleteClick,
  onEditClick,
  rowDataId,
  rowDeletingId,
  rowUpdatingId
}) => {
  const resources = useContext(ResourcesContext);
  return (
    <div className={styles.actionTemplate}>
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
            (rowDeletingId === rowDataId && isDeletingDocument) ||
            (rowUpdatingId === rowDataId && isUpdating) ||
            disabledButtons
          }
          icon={rowUpdatingId !== rowDataId || !isUpdating ? 'edit' : 'spinnerAnimate'}
          onClick={onEditClick}
          tooltip={resources.messages['edit']}
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
          tooltip={resources.messages['clone']}
          tooltipOptions={{ position: 'top' }}
          type="button"
        />
      )}
      {!isNil(onDeleteClick) && !hideDeletion && (
        <Button
          className={`${`p-button-rounded p-button-secondary-transparent ${styles.deleteRowButton}`} ${
            isDeletingDocument || (rowUpdatingId === rowDataId && isUpdating) || disabledButtons
              ? null
              : 'p-button-animated-blink'
          }`}
          disabled={isDeletingDocument || (rowUpdatingId === rowDataId && isUpdating) || disabledButtons}
          icon={rowDeletingId !== rowDataId || !isDeletingDocument ? 'trash' : 'spinnerAnimate'}
          onClick={onDeleteClick}
          tooltip={resources.messages['delete']}
          tooltipOptions={{ position: 'top' }}
          type="button"
        />
      )}
    </div>
  );
};

export { ActionsColumn };
