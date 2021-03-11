import React, { Fragment, useContext, useEffect, useReducer, useRef } from 'react';

import capitalize from 'lodash/capitalize';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './WebLinks.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dialog } from 'ui/views/_components/Dialog';
import { ErrorMessage } from 'ui/views/_components/ErrorMessage';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { RegularExpressions } from 'ui/views/_functions/Utils/RegularExpressions';
import { WebLinkService } from 'core/services/WebLink';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { webLinksReducer } from './functions/Reducers/webLinksReducer';

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

  const inputRef = useRef(null);

  const [webLinksState, webLinksDispatch] = useReducer(webLinksReducer, {
    deletingId: null,
    editingId: null,
    errors: { description: { message: '', hasErrors: false }, url: { message: '', hasErrors: false } },
    isAddOrEditWeblinkDialogVisible: false,
    isConfirmDeleteVisible: false,
    isDeleting: false,
    isLoading: false,
    isSubmitting: false,
    webLink: { id: undefined, description: '', url: '' },
    webLinksColumns: []
  });

  useEffect(() => {
    if (!isNil(inputRef.current)) inputRef.current.focus();
  }, [inputRef, webLinksState.isAddOrEditWeblinkDialogVisible]);

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

    webLinksDispatch({ type: 'SET_WEB_LINKS_COLUMNS', payload: { webLinksColumns: webLinkColArray } });
  }, [webLinks, webLinksState.webLink, isToolbarVisible]);

  const checkIsValidUrl = url => RegularExpressions['url'].test(url);

  const checkIsCorrectLength = inputValue => inputValue.length <= 255;

  const checkIsEmptyInput = inputValue => inputValue.trim() === '';

  const checkIsCorrectInputValue = inputName => {
    let hasErrors = false;
    let message = '';
    const inputValue = webLinksState.webLink[inputName];

    if (checkIsEmptyInput(inputValue)) {
      message = '';
      hasErrors = true;
    } else if (inputName === 'description' && !checkIsCorrectLength(inputValue)) {
      message = resources.messages['webLinkDescriptionValidationMax'];
      hasErrors = true;
    } else if (inputName === 'url' && !checkIsValidUrl(inputValue)) {
      message = resources.messages['urlError'];
      hasErrors = true;
    }

    setErrors(inputName, { message, hasErrors });

    return hasErrors;
  };

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

  const setErrors = (inputName, error) => {
    webLinksDispatch({ type: 'SET_ERRORS', payload: { inputName, error } });
  };

  const onDeleteWeblink = async () => {
    webLinksDispatch({ type: 'ON_DELETE_START', payload: { deletingId: webLinksState.webLink.id } });

    try {
      const weblinkToDelete = await WebLinkService.deleteWeblink(webLinksState.webLink);

      if (weblinkToDelete.isDeleted) {
        onLoadWebLinks();
      }
    } catch (error) {
      console.error('error', error);
    } finally {
      webLinksDispatch({ type: 'ON_DELETE_END' });
    }
  };

  const setIsAddOrEditWeblinkDialogVisible = isVisible => {
    webLinksDispatch({ type: 'SET_IS_ADD_OR_EDIT_WEB_LINK_DIALOG_VISIBLE', payload: isVisible });
  };

  const onHideAddEditDialog = () => {
    webLinksDispatch({ type: 'ON_HIDE_ADD_EDIT_DIALOG' });
  };

  const onDescriptionChange = inputValue => {
    webLinksDispatch({ type: 'ON_DESCRIPTION_CHANGE', payload: { description: inputValue } });
  };

  const onWeblinkUrlChange = inputValue => {
    webLinksDispatch({ type: 'ON_URL_CHANGE', payload: { url: inputValue } });
  };

  const onHideDeleteDialog = () => {
    webLinksDispatch({ type: 'ON_HIDE_DELETE_DIALOG' });
  };

  const setIsConfirmDeleteVisible = isVisible => {
    webLinksDispatch({
      type: 'SET_IS_CONFIRM_DELETE_VISIBLE',
      payload: { isConfirmDeleteVisible: isVisible }
    });
  };

  const onSaveRecord = async () => {
    checkIsCorrectInputValue('description');
    checkIsCorrectInputValue('url');

    if (!webLinksState.errors.url.hasErrors && !webLinksState.errors.description.hasErrors) {
      webLinksDispatch({ type: 'ON_SAVE_RECORD', payload: { webLink: webLinksState.webLink } });
      if (isNil(webLinksState.webLink.id)) {
        try {
          const newWeblink = await WebLinkService.create(dataflowId, webLinksState.webLink);

          if (newWeblink.isCreated.status >= 200 && newWeblink.isCreated.status <= 299) {
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
          webLinksDispatch({ type: 'SET_IS_SUBMITTING', payload: { isSubmitting: false } });
        }
      } else {
        webLinksDispatch({ type: 'ON_EDIT_RECORD_START', payload: { editingId: webLinksState.webLink.id } });
        try {
          const weblinkToEdit = await WebLinkService.update(dataflowId, webLinksState.webLink);

          if (weblinkToEdit.isUpdated.status >= 200 && weblinkToEdit.isUpdated.status <= 299) {
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
          webLinksDispatch({
            type: 'ON_EDIT_RECORD_END'
          });
        }
      }
    }
  };

  const getButtonIcon = () => {
    if (webLinksState.isSubmitting) {
      return 'spinnerAnimate';
    }

    if (isNil(webLinksState.webLink.id)) {
      return 'add';
    }

    return 'edit';
  };

  const webLinkEditButtons = webLink => {
    const getDeleteButtonIcon = () => {
      if (webLinksState.deletingId === webLink.id && webLinksState.isDeleting) {
        return 'spinnerAnimate';
      }
      return 'trash';
    };

    const getEditButtonIcon = () => {
      if (webLinksState.editingId === webLink.id && webLinksState.isEditing) {
        return 'spinnerAnimate';
      }
      return 'edit';
    };

    return (
      <div className={styles.webLinkEditButtons}>
        <Button
          className={`${`p-button-rounded p-button-secondary-transparent ${styles.editRowButton}`} p-button-animated-blink`}
          disabled={
            (webLinksState.editingId === webLink.id && webLinksState.isEditing) ||
            (webLinksState.deletingId === webLink.id && webLinksState.isDeleting)
          }
          icon={getEditButtonIcon()}
          onClick={() => {
            webLinksDispatch({
              type: 'SET_WEB_LINK',
              payload: { webLink: { description: webLink.description, id: webLink.id, url: webLink.url } }
            });
            setIsAddOrEditWeblinkDialogVisible(true);
          }}
          type="button"
        />

        <Button
          className={`${`p-button-rounded p-button-secondary-transparent ${styles.deleteRowButton}`} p-button-animated-blink`}
          disabled={
            (webLinksState.deletingId === webLink.id && webLinksState.isDeleting) ||
            (webLinksState.editingId === webLink.id && webLinksState.isEditing)
          }
          icon={getDeleteButtonIcon()}
          onClick={() => {
            webLinksDispatch({
              type: 'SET_WEB_LINK',
              payload: { webLink: { description: webLink.description, id: webLink.id, url: webLink.url } }
            });
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
                webLinksState.isLoading ? 'p-button-animated-spin' : ''
              }`}
              disabled={false}
              icon={'refresh'}
              label={resources.messages['refresh']}
              onClick={async () => {
                webLinksDispatch({
                  type: 'SET_IS_LOADING',
                  payload: { isLoading: true }
                });

                await onLoadWebLinks();

                webLinksDispatch({
                  type: 'SET_IS_LOADING',
                  payload: { isLoading: false }
                });
              }}
            />
          </div>
        </Toolbar>
      ) : (
        <></>
      )}
      <DataTable
        autoLayout={true}
        loading={webLinksState.isLoading}
        onSort={e => {
          setSortFieldWeblinks(e.sortField);
          setSortOrderWeblinks(e.sortOrder);
        }}
        paginator={false}
        selectionMode="single"
        sortField={sortFieldWeblinks}
        sortOrder={sortOrderWeblinks}
        value={webLinks}>
        {!isEmpty(webLinks) ? webLinksState.webLinksColumns : emptyWebLinkColumns}
      </DataTable>
      {webLinks.length === 0 && (
        <div className={styles.noDataWrapper}>
          <h4>{resources.messages['noWebLinks']}</h4>
        </div>
      )}

      {webLinksState.isAddOrEditWeblinkDialogVisible && (
        <Dialog
          blockScroll={false}
          className={styles.dialog}
          contentStyle={{ height: '80%', maxHeight: '80%', overflow: 'auto' }}
          header={
            isNil(webLinksState.webLink.id) ? resources.messages['createNewWebLink'] : resources.messages['editWebLink']
          }
          modal={true}
          onHide={() => onHideAddEditDialog()}
          style={{ width: '50%', height: '80%' }}
          visible={webLinksState.isAddOrEditWeblinkDialogVisible}>
          <form>
            <fieldset>
              <div className={`formField${webLinksState.errors.description.hasErrors ? ' error' : ''}`}>
                <input
                  id={`descriptionWebLinks`}
                  ref={inputRef}
                  maxLength={255}
                  onChange={e => {
                    onDescriptionChange(e.target.value);
                  }}
                  onBlur={() => checkIsCorrectInputValue('description')}
                  onKeyPress={e => {
                    if (e.key === 'Enter' && !checkIsCorrectInputValue('description')) {
                      onSaveRecord();
                    }
                  }}
                  name="description"
                  placeholder={resources.messages['description']}
                  type="text"
                  value={webLinksState.webLink.description}
                />
                <label htmlFor="descriptionWebLinks" className="srOnly">
                  {resources.messages['description']}
                </label>
                {webLinksState.errors.description.message !== '' && (
                  <ErrorMessage message={webLinksState.errors.description.message} />
                )}
              </div>

              <div className={`formField${webLinksState.errors.url.hasErrors ? ' error' : ''}`}>
                <input
                  id={`urlWebLinks`}
                  name="url"
                  onChange={e => onWeblinkUrlChange(e.target.value)}
                  onBlur={() => checkIsCorrectInputValue('url')}
                  onKeyPress={e => {
                    if (e.key === 'Enter' && !checkIsCorrectInputValue('url')) {
                      onSaveRecord();
                    }
                  }}
                  placeholder={resources.messages['url']}
                  type="text"
                  value={webLinksState.webLink.url}
                />
                <label htmlFor="urlWebLinks" className="srOnly">
                  {resources.messages['url']}
                </label>
                {webLinksState.errors.url.message !== '' && <ErrorMessage message={webLinksState.errors.url.message} />}
              </div>
            </fieldset>
            <fieldset>
              <div className={`${styles.buttonWrap} ui-dialog-buttonpane p-clearfix`}>
                <Button
                  onClick={() => onSaveRecord()}
                  label={isNil(webLinksState.webLink.id) ? resources.messages['add'] : resources.messages['edit']}
                  disabled={webLinksState.isSubmitting}
                  icon={getButtonIcon(webLinksState.isSubmitting)}
                />
                <Button
                  className={`${styles.cancelButton} p-button-secondary button-right-aligned`}
                  label={resources.messages['cancel']}
                  icon="cancel"
                  onClick={() => onHideAddEditDialog()}
                />
              </div>
            </fieldset>
          </form>
        </Dialog>
      )}

      {webLinksState.isConfirmDeleteVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          disabledConfirm={webLinksState.isDeleting}
          header={resources.messages['delete']}
          iconConfirm={webLinksState.isDeleting ? 'spinnerAnimate' : 'check'}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={e => onDeleteWeblink(e)}
          onHide={onHideDeleteDialog}
          visible={webLinksState.isConfirmDeleteVisible}>
          {resources.messages['deleteWebLink']}
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
