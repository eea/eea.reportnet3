import React, { useContext, useEffect, useState, useRef } from 'react';

import * as Yup from 'yup';
import { Formik, Form, Field } from 'formik';
import { isEmpty, isNull, isString, isUndefined } from 'lodash';

import styles from './WebLinks.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dialog } from 'ui/views/_components/Dialog';
import { InputText } from 'ui/views/_components/InputText';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { WebLinkService } from 'core/services/WebLink';

export const WebLinks = ({ isCustodian, dataflowId }) => {
  const resources = useContext(ResourcesContext);

  const [editedRecord, setEditedRecord] = useState({});
  const [isAddDialogVisible, setIsAddDialogVisible] = useState(false);
  const [isConfirmDeleteVisible, setIsConfirmDeleteVisible] = useState(false);
  const [isEditDialogVisible, setIsEditDialogVisible] = useState(false);
  const [isNewRecord, setIsNewRecord] = useState(false);
  const [newRecord, setNewRecord] = useState({ description: '', url: '' });
  const [selectedRecord, setSelectedRecord] = useState({});
  const [webLinksColumns, setWebLinksColumns] = useState([]);
  const [webLinks, setWebLinks] = useState();
  const [reload, setReload] = useState(false);

  const initialValues = { description: '', url: '' };
  const form = useRef(null);

  const addWeblinkSchema = Yup.object().shape({
    description: Yup.string().required(),
    url: Yup.string()
      .url()
      .required()
  });

  const onLoadWebLinks = async () => {
    // setIsLoading(true);
    try {
      setWebLinks(await WebLinkService.all(dataflowId));
    } catch (error) {
      if (error.response.status === 401 || error.response.status === 403) {
        console.log('error', error.response);
      }
    } finally {
      //setIsLoading(false);
    }
  };

  useEffect(() => {
    onLoadWebLinks();
  }, [reload]);

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

  /*  const addRowDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button label={resources.messages['save']} icon="save" onClick={() => onSaveRecord(newRecord)} />
      <Button
        className="p-button-secondary"
        label={resources.messages['cancel']}
        icon="cancel"
        onClick={() => {
          setIsAddDialogVisible(false);
        }}
      />
    </div>
  ); */

  /*   const newRecordForm = fieldsArray.map(column => {
    if (isAddDialogVisible) {
      return (
        <React.Fragment key={column.field}>
          <div className="p-col-4" style={{ padding: '.75em' }}>
            <label htmlFor={column.field}>
              {column.field === 'url'
                ? column.header.toUpperCase()
                : column.header.charAt(0).toUpperCase() + column.header.slice(1)}
            </label>
          </div>
          <div className="p-col-8" style={{ padding: '.5em' }}>
            <InputText
              id={column.field}
              onChange={e => onEditAddFormInput(column.field, e.target.value, column.field)}
            />
          </div>
        </React.Fragment>
      );
    }
  }); */

  const fieldsArray = [
    { field: 'description', header: resources.messages['description'] },
    { field: 'url', header: resources.messages['url'] }
  ];

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
      if (field === 'url') {
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
            setIsEditDialogVisible(false);
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
      if (i === 0) {
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
    if (isNewRecord) {
      try {
        const newWeblink = await WebLinkService.create(dataflowId, record);

        if (newWeblink.isCreated) {
          setReload(!reload);
        }

        setIsAddDialogVisible(false);
      } catch (error) {
        console.error('Error on save new Weblink: ', error);
      }
    } else {
      try {
        const weblinkToEdit = await WebLinkService.update(dataflowId, record);

        if (weblinkToEdit.isUpdated) {
          setReload(!reload);
        }

        setIsEditDialogVisible(false);
      } catch (error) {
        console.error('Error on update new Weblink: ', error);
        const errorResponse = error.response;
        console.error('errorResponse: ', errorResponse);
      }
    }
  };

  const onDeleteWeblink = async () => {
    const weblinkToDelete = await WebLinkService.deleteWeblink(selectedRecord);

    if (weblinkToDelete.isDeleted) {
      setReload(!reload);
    }

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

    if (isCustodian) {
      webLinkColArray = [webLinkEditionColumn, ...webLinkColArray];
    }
    setWebLinksColumns(webLinkColArray);
  }, [webLinks]);

  const emptyWebLinkColumns = fieldsArray.map(item => (
    <Column field={item.field} header={item.header} key={item.field} />
  ));

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
        footer={isCustodian ? addRowFooter : null}
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
        {!isEmpty(webLinks) ? webLinksColumns : emptyWebLinkColumns}
      </DataTable>

      <Dialog
        className={styles.dialog}
        blockScroll={false}
        contentStyle={{ height: '80%', maxHeight: '80%', overflow: 'auto' }}
        header={resources.messages['addNewRow']}
        modal={true}
        onHide={() => setIsAddDialogVisible(false)}
        style={{ width: '50%', height: '80%' }}
        visible={isAddDialogVisible}>
        <Formik ref={form} initialValues={initialValues} validationSchema={addWeblinkSchema} onSubmit={onSaveRecord}>
          {({ isSubmitting, errors, touched }) => (
            <Form>
              <fieldset>
                <div className={`formField${!isEmpty(errors.description) && touched.description ? ' error' : ''}`}>
                  <Field name="description" type="text" placeholder={resources.messages['description']} />
                </div>
                <div className={`formField${!isEmpty(errors.url) && touched.url ? ' error' : ''}`}>
                  <Field name="url" type="text" placeholder={resources.messages['url']} />
                </div>
              </fieldset>
              <fieldset>
                <hr />
                <div className={`${styles.buttonWrap} ui-dialog-buttonpane p-clearfix`}>
                  <Button
                    className={
                      !isEmpty(touched)
                        ? isEmpty(errors)
                          ? styles.primaryButton
                          : styles.disabledButton
                        : styles.disabledButton
                    }
                    label={resources.messages['add']}
                    disabled={isSubmitting}
                    icon="add"
                    type={isSubmitting ? '' : 'submit'}
                  />
                  <Button
                    className={`${styles.cancelButton} p-button-secondary`}
                    label={resources.messages['cancel']}
                    icon="cancel"
                    onClick={() => setIsAddDialogVisible(false)}
                  />
                </div>
              </fieldset>
            </Form>
          )}
        </Formik>
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
        onConfirm={e => onDeleteWeblink(e)}
        onHide={onHideDeleteDialog}
        visible={isConfirmDeleteVisible}>
        {resources.messages['deleteWebLink']}
      </ConfirmDialog>
    </>
  );
};
