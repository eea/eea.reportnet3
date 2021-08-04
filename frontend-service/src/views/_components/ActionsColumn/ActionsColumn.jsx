import isNil from 'lodash/isNil';

import styles from './ActionsColumn.module.scss';

import { Button } from 'views/_components/Button';

const ActionsColumn = ({
  disabledButtons,
  hideDeletion = false,
  hideEdition = false,
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
            disabledButtons ? null : 'p-button-animated-blink'
          }`}
          disabled={
            (rowDeletingId === rowDataId && isDeletingDocument) ||
            (rowUpdatingId === rowDataId && isUpdating) ||
            disabledButtons
          }
          icon={rowUpdatingId !== rowDataId || !isUpdating ? 'edit' : 'spinnerAnimate'}
          onClick={() => onEditClick()}
          type="button"
        />
      )}
      {!isNil(onDeleteClick) && !hideDeletion && (
        <Button
          className={`${`p-button-rounded p-button-secondary-transparent ${styles.deleteRowButton}`} ${
            disabledButtons ? null : 'p-button-animated-blink'
          }`}
          disabled={isDeletingDocument || (rowUpdatingId === rowDataId && isUpdating) || disabledButtons}
          icon={rowDeletingId !== rowDataId || !isDeletingDocument ? 'trash' : 'spinnerAnimate'}
          onClick={() => onDeleteClick()}
          type="button"
        />
      )}
    </div>
  );
};

export { ActionsColumn };
