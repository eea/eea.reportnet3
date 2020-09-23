import React from 'react';

import isNil from 'lodash/isNil';

import styles from './ActionsColumn.module.css';

import { Button } from 'ui/views/_components/Button';

const ActionsColumn = ({
  disabledButtons,
  hideDeletion = false,
  hideEdition = false,
  isDeletingDocument,
  onDeleteClick,
  onEditClick
}) => {
  return (
    <div className={styles.actionTemplate}>
      {!isNil(onEditClick) && !hideEdition && (
        <Button
          className={`${`p-button-rounded p-button-secondary-transparent ${styles.editRowButton}`} p-button-animated-blink`}
          disabled={isDeletingDocument || disabledButtons}
          icon="edit"
          onClick={!isDeletingDocument ? () => onEditClick() : null}
          type="button"
        />
      )}
      {!isNil(onDeleteClick) && !hideDeletion && (
        <Button
          className={`${`p-button-rounded p-button-secondary-transparent ${styles.deleteRowButton}`} p-button-animated-blink`}
          disabled={isDeletingDocument || disabledButtons}
          icon={!isDeletingDocument ? 'trash' : 'spinnerAnimate'}
          onClick={() => onDeleteClick()}
          type="button"
        />
      )}
    </div>
  );
};

export { ActionsColumn };
