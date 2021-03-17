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

import { TextUtils } from 'ui/views/_functions/Utils';

import { WebLinkService } from 'core/services/WebLink';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const WebLinks = ({
  dataflowId,
  isToolbarVisible,
  onLoadWebLinks,
  setSortFieldWeblinks,
  setSortOrderWeblinks,
  sortFieldWeblinks,
  sortOrderWeblinks,
  webLinks
}) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [deletingId, setDeletingId] = useState(null);
  const [editingId, setEditingId] = useState(null);
  const [isAddOrEditWeblinkDialogVisible, setIsAddOrEditWeblinkDialogVisible] = useState(false);
  const [isConfirmDeleteVisible, setIsConfirmDeleteVisible] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
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

    if (isToolbarVisible) webLinkColArray = [...webLinkColArray, webLinkEditionColumn];

    setWebLinksColumns(webLinkColArray);
  }, [webLinks, weblinkItem]);

  const addWeblinkSchema = Yup.object().shape({
    description: Yup.string().required(' ').max(255, resources.messages['webLinkDescriptionValidationMax']),
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

  const onResetValues = () => setWeblinkItem({ id: undefined, description: '', url: '' });

  const onDeleteWeblink = async () => {
    setIsDeleting(true);
    setDeletingId(weblinkItem.id);

    try {
      const { status } = await WebLinkService.deleteWebLink(weblinkItem);

      if (status >= 200 && status <= 299) {
        onLoadWebLinks();
      }
    } catch (error) {
      console.error('error', error);
    } finally {
      setIsConfirmDeleteVisible(false);
      onResetValues();
      setIsDeleting(false);
    }
  };

  const onHideAddEditDialog = () => {
    setIsAddOrEditWeblinkDialogVisible(false);
    onResetValues();
  };

  const onHideDeleteDialog = () => {
    setIsConfirmDeleteVisible(false);
    onResetValues();
  };

  const onSaveRecord = async (e, setSubmitting) => {
    setWeblinkItem(e);
    setSubmitting(true);
    if (isNil(weblinkItem.id)) {
      try {
        const newWeblink = await WebLinkService.create(dataflowId, e);

        if (newWeblink.status >= 200 && newWeblink.status <= 299) {
          onLoadWebLinks();
        }

        onHideAddEditDialog();
      } catch (error) {
        console.error('Error on save new Weblink: ', error);

        if (error.response.status === 400) {
          notificationContext.add({
            type: 'WRONG_WEBLINK_ERROR'
          });
        } else if (error.response.status === 409) {
          notificationContext.add({
            type: 'DUPLICATED_WEBLINK_ERROR'
          });
        }
      } finally {
        setSubmitting(false);
      }
    } else {
      setIsEditing(true);
      setEditingId(e.id);
      try {
        const weblinkToEdit = await WebLinkService.update(dataflowId, e);

        if (weblinkToEdit.status >= 200 && weblinkToEdit.status <= 299) {
          onLoadWebLinks();
        }

        onHideAddEditDialog();
      } catch (error) {
        console.error('Error on update new Weblink: ', error);

        if (error.response.status === 400) {
          notificationContext.add({
            type: 'WRONG_WEBLINK_ERROR'
          });
        } else if (error.response.status === 409) {
          notificationContext.add({
            type: 'DUPLICATED_WEBLINK_ERROR'
          });
        }
      } finally {
        setSubmitting(false);
        setIsEditing(false);
      }
    }
  };

  const getButtonIcon = isSubmitting => {
    if (isSubmitting) {
      return 'spinnerAnimate';
    }

    if (isNil(weblinkItem.id)) {
      return 'add';
    }

    return 'edit';
  };

  const webLinkEditButtons = weblink => {
    const getDeleteButtonIcon = () => {
      if (deletingId === weblink.id && isDeleting) {
        return 'spinnerAnimate';
      }
      return 'trash';
    };
    const getEditButtonIcon = () => {
      if (editingId === weblink.id && isEditing) {
        return 'spinnerAnimate';
      }
      return 'edit';
    };

    return (
      <div className={styles.webLinkEditButtons}>
        <Button
          className={`${`p-button-rounded p-button-secondary-transparent ${styles.editRowButton}`} p-button-animated-blink`}
          disabled={(editingId === weblink.id && isEditing) || (deletingId === weblink.id && isDeleting)}
          icon={getEditButtonIcon()}
          onClick={() => {
            setWeblinkItem(weblink);
            setIsAddOrEditWeblinkDialogVisible(true);
          }}
          type="button"
        />

        <Button
          className={`${`p-button-rounded p-button-secondary-transparent ${styles.deleteRowButton}`} p-button-animated-blink`}
          disabled={(deletingId === weblink.id && isDeleting) || (editingId === weblink.id && isEditing)}
          icon={getDeleteButtonIcon()}
          onClick={() => {
            setWeblinkItem(weblink);
            setIsConfirmDeleteVisible(true);
          }}
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
      {isToolbarVisible ? (
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
        onSort={e => {
          setSortFieldWeblinks(e.sortField);
          setSortOrderWeblinks(e.sortOrder);
        }}
        paginator={false}
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
          blockScroll={false}
          className={styles.dialog}
          contentStyle={{ height: '80%', maxHeight: '80%', overflow: 'auto' }}
          header={isNil(weblinkItem.id) ? resources.messages['createNewWebLink'] : resources.messages['editWebLink']}
          modal={true}
          onHide={() => onHideAddEditDialog()}
          style={{ width: '50%', height: '80%' }}
          visible={isAddOrEditWeblinkDialogVisible}>
          <Formik
            enableReinitialize={true}
            initialValues={weblinkItem}
            onSubmit={(e, actions) => {
              onSaveRecord(e, actions.setSubmitting);
            }}
            ref={form}
            validationSchema={addWeblinkSchema}>
            {({ isSubmitting, errors, touched, values }) => (
              <Form>
                <fieldset>
                  <div className={`formField${!isEmpty(errors.description) && touched.description ? ' error' : ''}`}>
                    <Field
                      autoFocus={true}
                      id={`descriptionWebLinks`}
                      innerRef={inputRef}
                      maxLength={255}
                      name="description"
                      placeholder={resources.messages['description']}
                      type="text"
                      value={values.description}
                    />
                    <label htmlFor="descriptionWebLinks" className="srOnly">
                      {resources.messages['description']}
                    </label>
                    <ErrorMessage className="error" name="description" component="div" />
                  </div>

                  <div className={`formField${!isEmpty(errors.url) && touched.url ? ' error' : ''}`}>
                    <Field
                      id={`urlWebLinks`}
                      name="url"
                      placeholder={resources.messages['url']}
                      type="text"
                      value={values.url}
                    />
                    <label htmlFor="urlWebLinks" className="srOnly">
                      {resources.messages['url']}
                    </label>
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
                      type={isSubmitting ? '' : 'submit'}
                      icon={getButtonIcon(isSubmitting)}
                    />
                    <Button
                      className={`${styles.cancelButton} p-button-secondary button-right-aligned`}
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

      {isConfirmDeleteVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          disabledConfirm={isDeleting}
          header={resources.messages['delete']}
          iconConfirm={isDeleting ? 'spinnerAnimate' : 'check'}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={e => onDeleteWeblink(e)}
          onHide={onHideDeleteDialog}
          visible={isConfirmDeleteVisible}>
          {resources.messages['deleteWebLink']}
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
