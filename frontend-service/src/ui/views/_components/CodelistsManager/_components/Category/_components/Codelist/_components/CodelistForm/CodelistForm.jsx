import React, { useContext, useEffect, useState } from 'react';
import { capitalize, isNull, isUndefined, isEmpty } from 'lodash';

import styles from './CodelistForm.module.css';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { InputText } from 'ui/views/_components/InputText';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const CodelistForm = ({
  columns,
  formType,
  onCancelAddEditItem,
  onChangeItemForm,
  onFormLoaded,
  onHideDialog,
  onSaveItem,
  item,
  visible
}) => {
  const resources = useContext(ResourcesContext);
  const [isSaveDisabled, setIsSaveDisabled] = useState(true);

  useEffect(() => {
    onFormLoaded();
  }, []);

  useEffect(() => {
    checkDisabledSave();
  }, [item]);

  const checkDisabledSave = () => {
    const inmKeys = [...Object.keys(item)];
    if (inmKeys.indexOf('codelistId') > -1) {
      inmKeys.splice(inmKeys.indexOf('codelistId'), 1);
    }
    console.log(inmKeys.filter(key => item[key].trim() === ''));
    setIsSaveDisabled(!isEmpty(inmKeys.filter(key => item[key].trim() === '')));
  };

  const codelistDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        disabled={isSaveDisabled}
        icon="save"
        label={resources.messages['save']}
        onClick={() => {
          setIsSaveDisabled(true);
          onSaveItem(formType);
          setIsSaveDisabled(false);
        }}
      />
      <Button
        icon="cancel"
        label={resources.messages['cancel']}
        onClick={() => {
          onCancelAddEditItem();
          onHideDialog();
        }}
      />
    </div>
  );

  const addEditCodelistForm = columns.map((column, i) => {
    return (
      <React.Fragment key={column}>
        <span className={`${styles.codelistInput} p-float-label`}>
          <InputText
            id={`${column}Input`}
            onChange={e => onChangeItemForm(column, e.target.value, formType)}
            value={isUndefined(item) || isNull(item[column]) || isUndefined(item[column]) ? '' : item[column]}
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
      footer={codelistDialogFooter}
      header={formType === 'EDIT' ? resources.messages['editItem'] : resources.messages['addNewItem']}
      modal={true}
      onHide={() => onHideDialog()}
      style={{ width: '50%' }}
      visible={visible}>
      <div className="p-grid p-fluid"> {addEditCodelistForm}</div>
    </Dialog>
  );

  return renderDialog;
};

export { CodelistForm };
