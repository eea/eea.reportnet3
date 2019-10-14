import React, { useState, useEffect, useContext } from 'react';

import { isArray } from 'lodash';

import styles from './WebLinks.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dialog } from 'ui/views/_components/Dialog';
import { InputText } from 'ui/views/_components/InputText';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

const WebLinks = ({ webLinks, onLoadDocumentsAndWebLinks, isCustodian }) => {
  const resources = useContext(ResourcesContext);

  const [editedRecord, setEditedRecord] = useState({});
  const [isAddDialogVisible, setIsAddDialogVisible] = useState(false);
  const [isConfirmDeleteVisible, setIsConfirmDeleteVisible] = useState(false);
  const [isEditDialogVisible, setIsEditDialogVisible] = useState(false);
  const [isNewRecord, setIsNewRecord] = useState(false);
  const [newRecord, setNewRecord] = useState({});
  const [webLinksColumns, setWebLinksColumns] = useState([]);

  const addRowHeader = (
    <div className="p-clearfix" style={{ width: '100%' }}>
      <Button
        style={{ float: 'left' }}
        label={resources.messages['add']}
        icon="add"
        onClick={() => {
          setIsNewRecord(true);
          setIsAddDialogVisible(true);
        }}
      />
    </div>
  );

  const addRowDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        label={resources.messages['cancel']}
        icon="cancel"
        onClick={() => {
          setIsAddDialogVisible(false);
        }}
      />
      <Button
        label={resources.messages['save']}
        icon="save"
        onClick={() => {
          onSaveRecord(newRecord);
        }}
      />
    </div>
  );

  const newRecordForm = webLinksColumns.map((column, i) => {
    console.log('column', column);
    if (isAddDialogVisible) {
      if (i == 0) {
        return;
      }
      return (
        <React.Fragment key={column.props.field}>
          <div className="p-col-4" style={{ padding: '.75em' }}>
            <label htmlFor={column.props.field}>{column.props.header.toUpperCase()}</label>
          </div>
          <div className="p-col-8" style={{ padding: '.5em' }}>
            <InputText
              id={column.props.field}
              onChange={e => onEditAddFormInput(column.props.field, e.target.value, column.props.field)}
            />
          </div>
        </React.Fragment>
      );
    }
  });

  const editRowDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button label={resources.messages['cancel']} icon="cancel" onClick />
      <Button
        label={resources.messages['save']}
        icon="save"
        onClick={() => {
          try {
            onSaveRecord(editedRecord);
          } catch (error) {
            console.error(error);
          }
        }}
      />
    </div>
  );

  const editRecordForm = webLinksColumns.map((column, i) => {
    console.log('column', column);
    if (isEditDialogVisible) {
      if (i == 0) {
        return;
      }
      return (
        <React.Fragment key={column.props.field}>
          <div className="p-col-4" style={{ padding: '.75em' }}>
            <label htmlFor={column.props.field}>{column.props.header.toUpperCase()}</label>
          </div>
          <div className="p-col-8" style={{ padding: '.5em' }}>
            <InputText
              id={column.props.field}
              onChange={e => onEditAddFormInput(column.props.field, e.target.value, column.props.field)}
            />
          </div>
        </React.Fragment>
      );
    }
  });

  const onEditAddFormInput = (input, value) => {
    if (!isNewRecord) {
      setEditedRecord({ ...editedRecord });
    } else {
      setNewRecord({ ...newRecord });
    }
  };

  const onSaveRecord = newRecord => console.log('saving ', newRecord);

  const onDeleteWeblink = () => {
    console.log('deleting web link');
  };

  const onHideDeleteDialog = () => {
    setIsConfirmDeleteVisible(false);
  };

  const webLinkEditButtons = () => {
    return (
      <div className={styles.webLinkEditButtons}>
        <Button
          type="button"
          icon="edit"
          className={`${`p-button-rounded p-button-secondary ${styles.editRowButton}`}`}
          onClick={() => setIsEditDialogVisible(true)}
        />
        <Button
          type="button"
          icon="trash"
          className={`${`p-button-rounded p-button-secondary ${styles.deleteRowButton}`}`}
          onClick={() => setIsConfirmDeleteVisible(true)}
        />
      </div>
    );
  };

  const webLinkEditionColumn = <Column key={'buttonsUniqueId'} body={row => webLinkEditButtons(row)} />;

  useEffect(() => {
    let webLinkKeys = isArray(webLinks) ? Object.keys(webLinks[0]) : [];
    let webLinkColArray = webLinkKeys.map(key => (
      <Column
        key={key}
        columnResizeMode="expand"
        field={key}
        filter={false}
        filterMatchMode="contains"
        header={key}
        body={key === 'url' ? linkTemplate : null}
      />
    ));

    if (isCustodian) {
      webLinkColArray = [webLinkEditionColumn, ...webLinkColArray];
    }
    setWebLinksColumns(webLinkColArray);
  }, [webLinks]);

  const linkTemplate = rowData => {
    return (
      <a href={rowData.url} target="_blank" rel="noopener noreferrer">
        {rowData.url}
      </a>
    );
  };

  return (
    <>
      <DataTable
        value={webLinks}
        autoLayout={true}
        paginator={true}
        rowsPerPageOptions={[5, 10, 100]}
        header={isCustodian ? addRowHeader : null}
        rows={10}>
        {webLinksColumns}
      </DataTable>

      <Dialog
        className={styles.dialog}
        blockScroll={false}
        contentStyle={{ height: '80%', maxHeight: '80%', overflow: 'auto' }}
        footer={addRowDialogFooter}
        header={resources.messages['addNewRow']}
        modal={true}
        onHide={() => setIsAddDialogVisible(false)}
        style={{ width: '50%', height: '80%' }}
        visible={isAddDialogVisible}>
        <div className="p-grid p-fluid">{newRecordForm}</div>
      </Dialog>

      <Dialog
        className={styles.dialog}
        blockScroll={false}
        contentStyle={{ height: '80%', maxHeight: '80%', overflow: 'auto' }}
        footer={editRowDialogFooter}
        header={resources.messages['editRow']}
        modal={true}
        onHide={() => setIsEditDialogVisible(false)}
        style={{ width: '50%', height: '80%' }}
        visible={isEditDialogVisible}>
        <div className="p-grid p-fluid">{editRecordForm}</div>
      </Dialog>

      <ConfirmDialog
        header={resources.messages['delete']}
        labelCancel={resources.messages['no']}
        labelConfirm={resources.messages['yes']}
        maximizable={false}
        onConfirm={() => onDeleteWeblink()}
        onHide={onHideDeleteDialog}
        visible={isConfirmDeleteVisible}>
        {resources.messages['deleteWebLink']}
      </ConfirmDialog>
    </>
  );
};

export { WebLinks };
