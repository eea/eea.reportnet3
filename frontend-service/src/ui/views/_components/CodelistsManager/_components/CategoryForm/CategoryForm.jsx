import React, { useContext } from 'react';
import { capitalize, isNull, isUndefined } from 'lodash';

import styles from './CategoryForm.module.css';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { InputText } from 'ui/views/_components/InputText';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const CategoryForm = ({
  checkCategoryDuplicates,
  columns,
  isIncorrect,
  newCategory,
  onChangeCategoryForm,
  onHideDialog,
  onSaveCategory,
  onToggleIncorrect,
  visible
}) => {
  const resources = useContext(ResourcesContext);
  const categoryDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        className="p-button-primary"
        disabled={isIncorrect || Object.values(newCategory).includes('')}
        icon="save"
        label={resources.messages['save']}
        onClick={() => {
          onSaveCategory();
        }}
      />
      <Button
        className="p-button-secondary"
        icon="cancel"
        label={resources.messages['cancel']}
        onClick={() => {
          onToggleIncorrect(false);
          onHideDialog();
        }}
      />
    </div>
  );

  const addCategoryForm = columns.map(column => {
    return (
      <React.Fragment key={column}>
        <span className={`${styles.categoryInput} p-float-label`}>
          <InputText
            className={isIncorrect && column === 'shortCode' ? styles.categoryIncorrectInput : null}
            id={`${column}Input`}
            onBlur={() =>
              column === 'shortCode' ? onToggleIncorrect(checkCategoryDuplicates(newCategory[column])) : null
            }
            onChange={e => onChangeCategoryForm(column, e.target.value)}
            // required={true}
            value={
              isUndefined(newCategory) || isNull(newCategory[column]) || isUndefined(newCategory[column])
                ? ''
                : newCategory[column]
            }
          />
          <label htmlFor={`${column}Input`}>
            {column === 'shortCode' ? resources.messages['categoryShortCode'] : capitalize(column)}
          </label>
        </span>
      </React.Fragment>
    );
  });

  const renderDialog = (
    <Dialog
      className="edit-table"
      blockScroll={false}
      footer={categoryDialogFooter}
      header={resources.messages['addNewCategory']}
      modal={true}
      onHide={() => onHideDialog()}
      style={{ width: '50%' }}
      visible={visible}>
      <div className="p-grid p-fluid"> {addCategoryForm}</div>
    </Dialog>
  );

  return renderDialog;
};

export { CategoryForm };
