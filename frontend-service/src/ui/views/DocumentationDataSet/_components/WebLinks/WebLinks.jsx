import React, { useContext, useEffect, useState, useRef } from 'react';

import * as Yup from 'yup';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import { capitalize, isEmpty, isUndefined } from 'lodash';

import styles from './WebLinks.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dialog } from 'ui/views/_components/Dialog';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Toolbar } from 'ui/views/_components/Toolbar';
import { WebLinkService } from 'core/services/WebLink';

export const WebLinks = ({ isCustodian, dataflowId, webLinks, onLoadWebLinks }) => {
  const resources = useContext(ResourcesContext);
  const [isAddOrEditWeblinkDialogVisible, setIsAddOrEditWeblinkDialogVisible] = useState(false);
  const [isConfirmDeleteVisible, setIsConfirmDeleteVisible] = useState(false);
  const [weblinkItem, setWeblinkItem] = useState({});
  const [reload, setReload] = useState(false);
  const [webLinksColumns, setWebLinksColumns] = useState([]);

  const form = useRef(null);
  const addWeblinkSchema = Yup.object().shape({
    description: Yup.string().required(),
    url: Yup.string()
      .matches(
        /^(sftp:\/\/www\.|sftp:\/\/|ftp:\/\/www\.|ftp:\/\/|http:\/\/www\.|https:\/\/www\.|http:\/\/|https:\/\/)?[a-z0-9]+([\-\.]{1}[a-z0-9]+)*\.[a-z]{2,63}(:[0-9]{1,5})?(\/.*)?$/,
        resources.messages['urlError']
      )
      .required(' ')
  });

  const resetForm = () => {
    setWeblinkItem({ id: undefined, description: '', url: '' });
    form.current.resetForm();
  };

  useEffect(() => {
    onLoadWebLinks();
    resetForm();
  }, [reload]);

  const onHideAddEditDialog = () => {
    setIsAddOrEditWeblinkDialogVisible(false);
    resetForm();
  };

  const getValidUrl = (url = '') => {
    let newUrl = window.decodeURIComponent(url);
    newUrl = newUrl.trim().replace(/\s/g, '');

    if (/^(:\/\/)/.test(newUrl)) {
      return `http${newUrl}`;
    }
    if (!/^(f|ht)tps?:\/\//i.test(newUrl)) {
      return `//${newUrl}`;
    }

    return newUrl;
  };

  const fieldsArray = [
    { field: 'description', header: resources.messages['description'] },
    { field: 'url', header: resources.messages['url'] }
  ];

  const onSaveRecord = async e => {
    if (isUndefined(weblinkItem.id)) {
      setWeblinkItem(e);

      try {
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
        setWeblinkItem(e);

        const weblinkToEdit = await WebLinkService.update(dataflowId, e);

        if (weblinkToEdit.isUpdated) {
          setReload(!reload);
        }

        onHideAddEditDialog();
      } catch (error) {
        console.error('Error on update new Weblink: ', error);
      }
    }
  };

  const onDeleteWeblink = async () => {
    const weblinkToDelete = await WebLinkService.deleteWeblink(weblinkItem);

    if (weblinkToDelete.isDeleted) {
      setReload(!reload);
    }

    setIsConfirmDeleteVisible(false);
  };

  const onHideDeleteDialog = () => {
    setIsConfirmDeleteVisible(false);
    resetForm();
  };

  const webLinkEditButtons = () => {
    return (
      <div className={styles.webLinkEditButtons}>
        <Button
          type="button"
          icon="edit"
          className={`${`p-button-rounded p-button-secondary ${styles.editRowButton}`}`}
          onClick={e => {
            setIsAddOrEditWeblinkDialogVisible(true);
          }}
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
          header={key === 'url' ? key.toUpperCase() : capitalize(key)}
          body={key === 'url' ? linkTemplate : null}
          sortable={true}
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
      <a href={getValidUrl(rowData.url)} target="_blank" rel="noopener noreferrer">
        {rowData.url}
      </a>
    );
  };

  return (
    <>
      {isCustodian ? (
        <Toolbar>
          <div className="p-toolbar-group-left">
            <Button
              className={`p-button-rounded p-button-secondary`}
              style={{ float: 'left' }}
              label={resources.messages['add']}
              icon="add"
              onClick={() => {
                setIsAddOrEditWeblinkDialogVisible(true);
              }}
            />
          </div>
        </Toolbar>
      ) : (
        <></>
      )}
      <DataTable
        autoLayout={true}
        onRowSelect={e => {
          setWeblinkItem(Object.assign({}, e.data));
        }}
        paginator={false}
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
        header={
          isUndefined(weblinkItem.id) ? resources.messages['createNewWebLink'] : resources.messages['editWebLink']
        }
        modal={true}
        onHide={() => onHideAddEditDialog()}
        style={{ width: '50%', height: '80%' }}
        visible={isAddOrEditWeblinkDialogVisible}>
        <Formik
          enableReinitialize={true}
          ref={form}
          initialValues={weblinkItem}
          validationSchema={addWeblinkSchema}
          onSubmit={e => {
            onSaveRecord(e);
          }}>
          {({ isSubmitting, errors, touched, values }) => (
            <Form>
              <fieldset>
                <div className={`formField${!isEmpty(errors.description) && touched.description ? ' error' : ''}`}>
                  <Field
                    name="description"
                    type="text"
                    placeholder={resources.messages['description']}
                    value={values.description}
                  />
                </div>
                <div className={`formField${!isEmpty(errors.url) && touched.url ? ' error' : ''}`}>
                  <Field name="url" type="text" placeholder={resources.messages['url']} value={values.url} />
                  <ErrorMessage name="url" component="div" />
                </div>
              </fieldset>
              <fieldset>
                <div className={`${styles.buttonWrap} ui-dialog-buttonpane p-clearfix`}>
                  <Button
                    className={
                      !isEmpty(touched)
                        ? isEmpty(errors)
                          ? styles.primaryButton
                          : styles.disabledButton
                        : styles.disabledButton
                    }
                    label={isUndefined(weblinkItem.id) ? resources.messages['add'] : resources.messages['edit']}
                    disabled={isSubmitting}
                    icon={isUndefined(weblinkItem.id) ? 'add' : 'edit'}
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
