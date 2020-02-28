import React from 'react';

import styles from './ActionsColumn.module.css';

import { Button } from 'ui/views/_components/Button';

const ActionsColumn = ({ onDeleteClick, onEditClick }) => {
  return (
    <div className={styles.actionTemplate}>
      <Button
        type="button"
        icon="edit"
        className={`${`p-button-rounded p-button-secondary-transparent ${styles.editRowButton}`} p-button-animated-blink`}
        onClick={() => onEditClick()}
      />
      <Button
        type="button"
        icon="trash"
        className={`${`p-button-rounded p-button-secondary-transparent ${styles.deleteRowButton}`} p-button-animated-blink`}
        onClick={() => onDeleteClick()}
      />
    </div>
  );
};

export { ActionsColumn };
