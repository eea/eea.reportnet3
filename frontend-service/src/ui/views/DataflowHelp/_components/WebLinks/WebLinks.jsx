import React, { Fragment, useContext, useEffect, useRef, useState } from 'react';

import * as Yup from 'yup';
import { Formik, Form, Field, ErrorMessage } from 'formik';

import capitalize from 'lodash/capitalize';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './WebLinks.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dialog } from 'ui/views/_components/Dialog';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { WebLinkService } from 'core/services/WebLink';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const WebLinks = ({
  dataflowId,
  isCustodian,
  onLoadWebLinks,
  setSortFieldWeblinks,
  setSortOrderWeblinks,
  sortFieldWeblinks,
  sortOrderWeblinks,
  webLinks
}) => {
  const resources = useContext(ResourcesContext);

  const [isAddOrEditWeblinkDialogVisible, setIsAddOrEditWeblinkDialogVisible] = useState(false);
  const [isConfirmDeleteVisible, setIsConfirmDeleteVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [weblinkItem, setWeblinkItem] = useState({ id: undefined, description: '', url: '' });
  const [webLinksColumns, setWebLinksColumns] = useState([]);

  const form = useRef(null);
  const inputRef = useRef(null);

  useEffect(() => {
    if (!isNil(form.current)) form.current.resetForm();
  }, [form.current, isAddOrEditWeblinkDialogVisible]);

  useEffect(() => {
    if (inputRef.current) inputRef.current.focus();
  }, [inputRef.current, isAddOrEditWeblinkDialogVisible]);

  useEffect(() => {
    let webLinkKeys = !isEmpty(webLinks) ? Object.keys(webLinks[0]) : [];
    let webLinkColArray = webLinkKeys
      .filter(key => key !== 'id')
      .map(key => (
        <Column
          body={key === 'url' ? linkTemplate : null}
          columnResizeMode="expand"
          field={key}
          filter={false}
          filterMatchMode="contains"
          header={key === 'url' ? key.toUpperCase() : capitalize(key)}
          key={key}
          sortable={true}
        />
      ));

    if (isCustodian) webLinkColArray = [...webLinkColArray, webLinkEditionColumn];

    setWebLinksColumns(webLinkColArray);
  }, [webLinks]);

  const addWeblinkSchema = Yup.object().shape({
    description: Yup.string().required(' '),
    url: Yup.string()
      .lowercase()
      .matches(
        /^(sftp:\/\/www\.|sftp:\/\/|ftp:\/\/www\.|ftp:\/\/|http:\/\/www\.|https:\/\/www\.|http:\/\/|https:\/\/)?[a-z0-9]+([\-\.]{1}[a-z0-9]+)*\.[a-z]{2,63}(:[0-9]{1,5})?(\/.*)?$/,
        resources.messages['urlError']
      )
      .required(' ')
  });

  const fieldsArray = [
    { field: 'description', header: resources.messages['description'] },
    { field: 'url', header: resources.messages['url'] }
  ];

  const emptyWebLinkColumns = fieldsArray.map(item => (
    <Column field={item.field} header={item.header} key={item.field} />
  ));

  const getValidUrl = (url = '') => {
    let newUrl = window.decodeURIComponent(url);
    newUrl = newUrl.trim().replace(/\s/g, '');

    if (/^(:\/\/)/.test(newUrl)) return `http${newUrl}`;

    if (!/^(f|ht)tps?:\/\//i.test(newUrl)) return `//${newUrl}`;

    return newUrl;
  };

  const linkTemplate = rowData => (
    <a href={getValidUrl(rowData.url)} target="_blank" rel="noopener noreferrer">
      {rowData.url}
    </a>
  );

  const onDeleteWeblink = async () => {
    const weblinkToDelete = await WebLinkService.deleteWeblink(weblinkItem);
    if (weblinkToDelete.isDeleted) {
      onLoadWebLinks();
    }
    setIsConfirmDeleteVisible(false);
  };

  const onHideAddEditDialog = () => {
    setIsAddOrEditWeblinkDialogVisible(false);
    onResetValues();
  };

  const onHideDeleteDialog = () => {
    setIsConfirmDeleteVisible(false);
    onResetValues();
  };

  const onResetValues = () => setWeblinkItem({ id: undefined, description: '', url: '' });

  const onSaveRecord = async e => {
    if (isNil(weblinkItem.id)) {
      setWeblinkItem(e);

      try {
        const newWeblink = await WebLinkService.create(dataflowId, e);

        if (newWeblink.isCreated) {
          onLoadWebLinks();
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
          onLoadWebLinks();
        }

        onHideAddEditDialog();
      } catch (error) {
        console.error('Error on update new Weblink: ', error);
      }
    }
  };

  const webLinkEditButtons = () => {
    return (
      <div className={styles.webLinkEditButtons}>
        <Button
          className={`${`p-button-rounded p-button-secondary-transparent ${styles.editRowButton}`} p-button-animated-blink`}
          icon="edit"
          onClick={() => setIsAddOrEditWeblinkDialogVisible(true)}
          type="button"
        />
        <Button
          className={`${`p-button-rounded p-button-secondary-transparent ${styles.deleteRowButton}`} p-button-animated-blink`}
          icon="trash"
          onClick={() => setIsConfirmDeleteVisible(true)}
          type="button"
        />
      </div>
    );
  };

  const webLinkEditionColumn = (
    <Column key={'buttonsUniqueId'} body={row => webLinkEditButtons(row)} style={{ width: '5em' }} />
  );

  return (
    <Fragment>
      {isCustodian ? (
        <Toolbar className={styles.weblinksToolbar}>
          <div className="p-toolbar-group-left">
            <Button
              className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink dataflowHelp-weblink-upload-help-step`}
              icon="add"
              label={resources.messages['add']}
              onClick={() => {
                setIsAddOrEditWeblinkDialogVisible(true);
              }}
              style={{ float: 'left' }}
            />
          </div>
          <div className="p-toolbar-group-right">
            <Button
              className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${
                isLoading ? 'p-button-animated-spin' : ''
              }`}
              disabled={false}
              icon={'refresh'}
              label={resources.messages['refresh']}
              onClick={async () => {
                setIsLoading(true);
                await onLoadWebLinks();
                setIsLoading(false);
              }}
            />
          </div>
        </Toolbar>
      ) : (
        <></>
      )}
      <DataTable
        autoLayout={true}
        loading={isLoading}
        onRowSelect={e => setWeblinkItem(Object.assign({}, e.data))}
        onSort={e => {
          setSortFieldWeblinks(e.sortField);
          setSortOrderWeblinks(e.sortOrder);
        }}
        paginator={false}
        rows={10}
        rowsPerPageOptions={[5, 10, 100]}
        selectionMode="single"
        sortField={sortFieldWeblinks}
        sortOrder={sortOrderWeblinks}
        value={webLinks}>
        {!isEmpty(webLinks) ? webLinksColumns : emptyWebLinkColumns}
      </DataTable>
      {webLinks.length === 0 && (
        <div className={styles.noDataWrapper}>
          <h4>{resources.messages['noWebLinks']}</h4>
        </div>
      )}
      {isAddOrEditWeblinkDialogVisible && (
        <Dialog
          className={styles.dialog}
          blockScroll={false}
          contentStyle={{ height: '80%', maxHeight: '80%', overflow: 'auto' }}
          header={isNil(weblinkItem.id) ? resources.messages['createNewWebLink'] : resources.messages['editWebLink']}
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
                      autoFocus={true}
                      innerRef={inputRef}
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
                      label={isNil(weblinkItem.id) ? resources.messages['add'] : resources.messages['edit']}
                      disabled={isSubmitting}
                      icon={isNil(weblinkItem.id) ? 'add' : 'edit'}
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
      )}

      <ConfirmDialog
        classNameConfirm={'p-button-danger'}
        header={resources.messages['delete']}
        labelCancel={resources.messages['no']}
        labelConfirm={resources.messages['yes']}
        maximizable={false}
        onConfirm={e => onDeleteWeblink(e)}
        onHide={onHideDeleteDialog}
        visible={isConfirmDeleteVisible}>
        {resources.messages['deleteWebLink']}
      </ConfirmDialog>
    </Fragment>
  );
};
