import React, { useContext, useEffect, useState } from 'react';

import { isEmpty, isNull, isString, isUndefined } from 'lodash';

import styles from './WebLinks.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dialog } from 'ui/views/_components/Dialog';
import { InputText } from 'ui/views/_components/InputText';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

export const WebLinks = ({ webLinks, isCustodian }) => {
  const resources = useContext(ResourcesContext);

  const [editedRecord, setEditedRecord] = useState({});
  const [isAddDialogVisible, setIsAddDialogVisible] = useState(false);
  const [isConfirmDeleteVisible, setIsConfirmDeleteVisible] = useState(false);
  const [isEditDialogVisible, setIsEditDialogVisible] = useState(false);
  const [isNewRecord, setIsNewRecord] = useState(false);
  const [newRecord, setNewRecord] = useState({ description: '', url: '' });
  const [selectedRecord, setSelectedRecord] = useState({});
  const [webLinksColumns, setWebLinksColumns] = useState([]);

  const onSelectRecord = val => {
    setIsNewRecord(false);
    setSelectedRecord({ ...val });
    setEditedRecord({ ...val });
  };

  const addRowFooter = (
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
        label={resources.messages['save']}
        icon="save"
        onClick={() => {
          onSaveRecord(newRecord);
        }}
      />
      <Button
        className="p-button-secondary"
        label={resources.messages['cancel']}
        icon="cancel"
        onClick={() => {
          setIsAddDialogVisible(false);
        }}
      />
    </div>
  );

  const newRecordForm = webLinksColumns.map((column, i) => {
    if (isAddDialogVisible) {
      if (i == 0 || i == 3) {
        return;
      }
      return (
        <React.Fragment key={column.props.field}>
          <div className="p-col-4" style={{ padding: '.75em' }}>
            <label htmlFor={column.props.field}>
              {column.props.header === 'url'
                ? column.props.header.toUpperCase()
                : column.props.header.charAt(0).toUpperCase() + column.props.header.slice(1)}
            </label>
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

  const onEditAddFormInput = (field, value) => {
    let record = {};
    if (!isNewRecord) {
      value = changeRecordValue(field, value);
      record = { ...editedRecord, [field]: value };
      setEditedRecord(record);
    } else {
      value = changeRecordValue(field, value);
      record = { ...newRecord, [field]: value };
      setNewRecord(record);
    }
  };

  const changeRecordValue = (field, value) => {
    if (!isUndefined(value) && !isNull(value) && isString(value)) {
      if (field == 'url') {
        value = value
          .replace(`\r`, '')
          .replace(`\n`, '')
          .replace(/\s/g, '');
      }
    }
    return value;
  };

  const editRowDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
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
      <Button
        className="p-button-secondary"
        label={resources.messages['cancel']}
        icon="cancel"
        onClick={() => setIsEditDialogVisible(false)}
      />
    </div>
  );

  const editRecordForm = webLinksColumns.map((column, i) => {
    if (isEditDialogVisible) {
      if (i == 0) {
        return;
      }
      return (
        <React.Fragment key={column.props.field}>
          <div className="p-col-4" style={{ padding: '.75em' }}>
            <label htmlFor={column.props.field}>
              {column.props.header === 'url'
                ? column.props.header.toUpperCase()
                : column.props.header.charAt(0).toUpperCase() + column.props.header.slice(1)}
            </label>
          </div>
          <div className="p-col-8" style={{ padding: '.5em' }}>
            <InputText
              id={column.props.field}
              value={editedRecord[column.props.field]}
              onChange={e => {
                return onEditAddFormInput(column.props.field, e.target.value, column.props.field);
              }}
            />
          </div>
        </React.Fragment>
      );
    }
  });

  const onSaveRecord = async record => {
    console.log('Saving record:', record);
    if (isNewRecord) {
      try {
        setIsAddDialogVisible(false);
      } catch (error) {
        console.error('DataViewer error: ', error);
        const errorResponse = error.response;
        console.error('DataViewer errorResponse: ', errorResponse);
      } finally {
      }
    } else {
      try {
        setIsEditDialogVisible(false);
      } catch (error) {
        console.error('DataViewer error: ', error);
        const errorResponse = error.response;
        console.error('DataViewer errorResponse: ', errorResponse);
      } finally {
      }
    }
  };

  const onDeleteWeblink = () => {
    console.log('deleting web link', selectedRecord['id']);
    setIsConfirmDeleteVisible(false);
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
    let webLinkKeys = !isEmpty(webLinks) ? Object.keys(webLinks[0]) : [];
    let webLinkColArray = webLinkKeys
      .filter(key => key !== 'id')
      .map(key => (
        <Column
          key={key}
          columnResizeMode="expand"
          field={key}
          filter={false}
          filterMatchMode="contains"
          sortable={true}
          header={key === 'url' ? key.toUpperCase() : key.charAt(0).toUpperCase() + key.slice(1)}
          body={key === 'url' ? linkTemplate : null}
        />
      ));

    // if (isCustodian) {
    if (false) {
      webLinkColArray = [webLinkEditionColumn, ...webLinkColArray];
    }
    setWebLinksColumns(webLinkColArray);
  }, [webLinks]);

  const emptyWebLink = [
    { field: 'description', header: resources.messages['description'] },
    { field: 'url', header: resources.messages['url'] }
  ].map(item => <Column field={item.field} header={item.header} />);

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
        autoLayout={true}
        editable={true}
        // footer={isCustodian ? addRowFooter : null}
        onContextMenuSelectionChange={() => {
          onSelectRecord(webLinks);
        }}
        onRowSelect={e => {
          return onSelectRecord(Object.assign({}, e.data));
        }}
        paginator={true}
        rows={10}
        rowsPerPageOptions={[5, 10, 100]}
        selectionMode="single"
        value={webLinks}>
        {!isEmpty(webLinks) ? webLinksColumns : emptyWebLink}
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
