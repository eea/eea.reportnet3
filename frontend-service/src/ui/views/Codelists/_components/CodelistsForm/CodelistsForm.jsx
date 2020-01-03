import React, { useContext } from 'react';
import { capitalize, isNull, isUndefined } from 'lodash';

import styles from './CodelistsForm.module.css';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { InputText } from 'ui/views/_components/InputText';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const CodelistsForm = ({ newCategory, columns, onChangeCategoryForm, onHideDialog, onSaveCategory, visible }) => {
  const resources = useContext(ResourcesContext);
  const categoryDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        label={resources.messages['save']}
        icon="save"
        onClick={() => {
          onSaveCategory();
        }}
      />
      <Button label={resources.messages['cancel']} icon="cancel" onClick={() => onHideDialog()} />
    </div>
  );

  const addCategoryForm = columns.map(column => {
    return (
      <React.Fragment key={column}>
        <span className={`${styles.categoryInput} p-float-label`}>
          <InputText
            id={`${column}Input`}
            onChange={e => onChangeCategoryForm(column, e.target.value)}
            // required={true}
            value={
              isUndefined(newCategory) || isNull(newCategory[column]) || isUndefined(newCategory[column])
                ? ''
                : newCategory[column]
            }
          />
          <label htmlFor={`${column}Input`}>{capitalize(column)}</label>
        </span>
      </React.Fragment>
    );
  });

  const renderDialog = (
    <Dialog
      className="edit-table"
      blockScroll={false}
      contentStyle={{ height: '80%', maxHeight: '80%', overflow: 'auto' }}
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

export { CodelistsForm };
