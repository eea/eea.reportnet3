import React, { useContext } from 'react';
import { capitalize, isNull } from 'lodash';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { InputText } from 'ui/views/_components/InputText';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const CodelistForm = ({
  columns,
  formType,
  onCancelAddEditItem,
  onChangeItemForm,
  onHideDialog,
  onSaveItem,
  items,
  visible
}) => {
  const resources = useContext(ResourcesContext);

  const codelistDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        label={resources.messages['save']}
        icon="save"
        onClick={() => {
          onSaveItem();
        }}
      />
      <Button
        label={resources.messages['cancel']}
        icon="cancel"
        onClick={() => {
          onCancelAddEditItem();
          onHideDialog();
        }}
      />
    </div>
  );

  const newCodelistForm = columns.map((column, i) => {
    //   if (i < colsSchema.length - 2) {
    console.log({ items });
    // const field = items[column];
    return (
      <React.Fragment key={column}>
        <div className="p-col-4" style={{ padding: '.75em' }}>
          <label htmlFor={column}>{capitalize(column)}</label>
        </div>
        <div className="p-col-8" style={{ padding: '.5em' }}>
          <InputText id={column} onChange={e => onChangeItemForm(column, e.target.value, formType)} />
        </div>
      </React.Fragment>
      //   }
    );
  });

  const renderDialog = (
    <Dialog
      className="edit-table"
      blockScroll={false}
      contentStyle={{ height: '80%', maxHeight: '80%', overflow: 'auto' }}
      footer={codelistDialogFooter}
      header={formType === 'EDIT' ? resources.messages['editRow'] : resources.messages['addNewRow']}
      modal={true}
      onHide={() => onHideDialog()}
      style={{ width: '50%', height: '80%' }}
      visible={visible}>
      <div className="p-grid p-fluid"> {formType === 'EDIT' ? newCodelistForm : newCodelistForm}</div>
    </Dialog>
  );

  return renderDialog;
};

export { CodelistForm };
