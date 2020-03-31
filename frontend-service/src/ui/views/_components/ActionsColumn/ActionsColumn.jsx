import React from 'react';

import isNil from 'lodash/isNil';

import styles from './ActionsColumn.module.css';

import { Button } from 'ui/views/_components/Button';

const ActionsColumn = ({ isDeletingDocument, onDeleteClick, onEditClick }) => {
  return (
    <div className={styles.actionTemplate}>
      {!isNil(onEditClick) && (
        <Button
          className={`${`p-button-rounded p-button-secondary-transparent ${styles.editRowButton}`} p-button-animated-blink`}
          disabled={isDeletingDocument}
          icon="edit"
          onClick={!isDeletingDocument ? () => onEditClick() : null}
          type="button"
        />
      )}
      {!isNil(onDeleteClick) && (
        <Button
          className={`${`p-button-rounded p-button-secondary-transparent ${styles.deleteRowButton}`} p-button-animated-blink`}
          disabled={isDeletingDocument}
          icon={!isDeletingDocument ? 'trash' : 'spinnerAnimate'}
          onClick={() => onDeleteClick()}
          type="button"
        />
      )}
    </div>
  );
};

export { ActionsColumn };
