import React, { useContext, useEffect, useState, useRef } from 'react';

import * as Yup from 'yup';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import { isEmpty } from 'lodash';

import styles from './WebLinks.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dialog } from 'ui/views/_components/Dialog';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { WebLinkService } from 'core/services/WebLink';

export const WebLinks = ({ isCustodian, dataflowId }) => {
  const resources = useContext(ResourcesContext);

  const [isAddEditDialogVisible, setIsAddEditDialogVisible] = useState(false);
  const [isConfirmDeleteVisible, setIsConfirmDeleteVisible] = useState(false);
  const [isNewRecord, setIsNewRecord] = useState(false);
  const [newRecord, setNewRecord] = useState({ id: undefined, description: '', url: '' });
  const [reload, setReload] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState({});
  const [webLinksColumns, setWebLinksColumns] = useState([]);
  const [webLinks, setWebLinks] = useState();

  const form = useRef(null);

  const initialValues = { description: '', url: '' };
  const addWeblinkSchema = Yup.object().shape({
    description: Yup.string().required(),
    url: Yup.string()
      // .url()
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
    setSelectedRecord(val);
    console.log('on selected record val', val, selectedRecord);
  };

  const onHideAddEditDialog = () => {
    form.current.resetForm();
    setIsAddEditDialogVisible(false);
    setIsNewRecord(false);
  };

  const addRowFooter = (
    <div className="p-clearfix" style={{ width: '100%' }}>
      <Button
        style={{ float: 'left' }}
        label={resources.messages['add']}
        icon="add"
        onClick={() => {
          setIsNewRecord(true);
          setIsAddEditDialogVisible(true);
        }}
      />
    </div>
  );

  const fieldsArray = [
    { field: 'description', header: resources.messages['description'] },
    { field: 'url', header: resources.messages['url'] }
  ];

  const onSaveRecord = async e => {
    console.log('e', e);

    if (isNewRecord) {
      try {
        console.log('onSaveRecord isNewRecord', isNewRecord);

        const newWeblink = await WebLinkService.create(dataflowId, e);

        if (newWeblink.isCreated) {
          setReload(!reload);
        }

        onHideAddEditDialog();
      } catch (error) {
        console.error('Error on save new Weblink: ', error);
      }
    } else {
      try {
        console.log('onSaveRecord when update. isNewRecord ', isNewRecord);

        const editedRecord = { ...selectedRecord, e };
        const weblinkToEdit = await WebLinkService.update(dataflowId, editedRecord);

        if (weblinkToEdit.isUpdated) {
          setReload(!reload);
        }

        onHideAddEditDialog();
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
          onClick={() => setIsAddEditDialogVisible(true)}
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

  const webLinkEditionColumn = (
    <Column key={'buttonsUniqueId'} body={row => webLinkEditButtons(row)} style={{ width: '5em' }} />
  );

  const onAddOrEditInputChange = e => {
    console.log('isNewRecord', isNewRecord);
    if (isNewRecord) {
      setNewRecord({ ...newRecord, [e.target.name]: e.target.value });
    } else {
      setSelectedRecord({ ...selectedRecord, [e.target.name]: e.target.value });
    }
  };

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
          onSelectRecord(Object.assign({}, e.data));
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
        header={isNewRecord ? resources.messages['addNewRow'] : resources.messages['editRow']}
        modal={true}
        onHide={() => onHideAddEditDialog()}
        style={{ width: '50%', height: '80%' }}
        visible={isAddEditDialogVisible}>
        <Formik
          ref={form}
          initialValues={initialValues}
          validationSchema={addWeblinkSchema}
          onSubmit={e => {
            onSaveRecord(e, isNewRecord ? newRecord : selectedRecord);
          }}>
          {({ isSubmitting, errors, touched }) => (
            <Form>
              {console.log(
                'isNewRecord ? newRecord : selectedRecord',
                isNewRecord,
                isNewRecord ? newRecord : selectedRecord
              )}
              <fieldset>
                <div className={`formField${!isEmpty(errors.description) && touched.description ? ' error' : ''}`}>
                  <Field
                    name="description"
                    type="text"
                    // onChange={e => onAddOrEditInputChange(e)}
                    placeholder={resources.messages['description']}
                    // value={isNewRecord ? initialValues.description : selectedRecord.description}
                  />
                  <ErrorMessage name="description" component="div" />
                </div>
                <div className={`formField${!isEmpty(errors.url) && touched.url ? ' error' : ''}`}>
                  <Field
                    name="url"
                    type="text"
                    // onChange={e => onAddOrEditInputChange(e)}
                    placeholder={resources.messages['url']}
                    // value={isNewRecord ? initialValues.url : selectedRecord.url}
                  />
                  <ErrorMessage name="url" component="div" />
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
                    label={isNewRecord ? resources.messages['add'] : resources.messages['edit']}
                    disabled={isSubmitting}
                    icon={isNewRecord ? 'add' : 'edit'}
                    type={isSubmitting ? '' : 'submit'}
                  />
                  <Button
                    className={`${styles.cancelButton} p-button-secondary`}
                    label={resources.messages['cancel']}
                    icon="cancel"
                    onClick={() => onHideAddEditDialog()}
                  />
                </div>
              </fieldset>
            </Form>
          )}
        </Formik>
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
