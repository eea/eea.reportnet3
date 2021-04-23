import { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';

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
import { Spinner } from 'ui/views/_components/Spinner';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { RegularExpressions } from 'ui/views/_functions/Utils/RegularExpressions';
import { WebLinkService } from 'core/services/WebLink';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { webLinksReducer } from './functions/Reducers/webLinksReducer';

export const WebLinks = ({
  dataflowId,
  isLoading,
  isToolbarVisible,
  onLoadWebLinks,
  setSortFieldWebLinks,
  setSortOrderWebLinks,
  sortFieldWebLinks,
  sortOrderWebLinks,
  webLinks
}) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const inputRef = useRef(null);

  const [webLinksState, webLinksDispatch] = useReducer(webLinksReducer, {
    editingId: null,
    errors: { description: { message: '', hasErrors: false }, url: { message: '', hasErrors: false } },
    isAddOrEditWebLinkDialogVisible: false,
    isConfirmDeleteVisible: false,
    isDeleting: false,
    isSubmitting: false,
    webLink: { id: undefined, description: '', url: '' },
    webLinksColumns: []
  });

  const [deletingId, setDeletingId] = useState('');

  useEffect(() => {
    if (!isNil(inputRef.current)) inputRef.current.focus();
  }, [inputRef, webLinksState.isAddOrEditWebLinkDialogVisible]);

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
  }, [webLinks, webLinksState.webLink, isToolbarVisible, isLoading]);

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

  const onDeleteWebLink = async id => {
    webLinksDispatch({ type: 'ON_DELETE_START' });
    setDeletingId(id);

    try {
      const { status } = await WebLinkService.deleteWebLink(webLinksState.webLink);

      if (status >= 200 && status <= 299) {
        onLoadWebLinks();
      }
    } catch (error) {
      console.error('Error on delete WebLink', error);

      notificationContext.add({
        type: 'DELETE_WEB_LINK_ERROR'
      });
    } finally {
      webLinksDispatch({ type: 'ON_DELETE_END' });
      setDeletingId('');
    }
  };

  const setIsAddOrEditWebLinkDialogVisible = isVisible => {
    webLinksDispatch({ type: 'SET_IS_ADD_OR_EDIT_WEB_LINK_DIALOG_VISIBLE', payload: isVisible });
  };

  const onHideAddEditDialog = () => {
    webLinksDispatch({ type: 'ON_HIDE_ADD_EDIT_DIALOG' });
  };

  const onDescriptionChange = inputValue => {
    webLinksDispatch({ type: 'ON_DESCRIPTION_CHANGE', payload: { description: inputValue } });
  };

  const onWebLinkUrlChange = inputValue => {
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
          const { status } = await WebLinkService.create(dataflowId, webLinksState.webLink);

          if (status >= 200 && status <= 299) {
            onLoadWebLinks();
          }

          onHideAddEditDialog();
        } catch (error) {
          console.error('Error on save new WebLink: ', error);

          if (error.response.status === 400) {
            notificationContext.add({
              type: 'WRONG_WEB_LINK_ERROR'
            });
          } else if (error.response.status === 409) {
            notificationContext.add({
              type: 'DUPLICATED_WEB_LINK_ERROR'
            });
          } else {
            notificationContext.add({
              type: 'ADD_WEB_LINK_ERROR'
            });
          }
        } finally {
          webLinksDispatch({ type: 'SET_IS_SUBMITTING', payload: { isSubmitting: false } });
        }
      } else {
        webLinksDispatch({ type: 'ON_EDIT_RECORD_START', payload: { editingId: webLinksState.webLink.id } });
        try {
          const { status } = await WebLinkService.update(dataflowId, webLinksState.webLink);

          if (status >= 200 && status <= 299) {
            onLoadWebLinks();
          }

          onHideAddEditDialog();
        } catch (error) {
          console.error('Error on update new WebLink: ', error);

          if (error.response.status === 400) {
            notificationContext.add({
              type: 'WRONG_WEB_LINK_ERROR'
            });
          } else if (error.response.status === 409) {
            notificationContext.add({
              type: 'DUPLICATED_WEB_LINK_ERROR'
            });
          } else {
            notificationContext.add({
              type: 'EDIT_WEB_LINK_ERROR'
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
      if (deletingId === webLink.id) {
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
          disabled={(webLinksState.editingId === webLink.id && webLinksState.isEditing) || deletingId === webLink.id}
          icon={getEditButtonIcon()}
          onClick={() => {
            webLinksDispatch({
              type: 'SET_WEB_LINK',
              payload: { webLink }
            });
            setIsAddOrEditWebLinkDialogVisible(true);
          }}
          type="button"
        />

        <Button
          className={`${`p-button-rounded p-button-secondary-transparent ${styles.deleteRowButton}`} p-button-animated-blink`}
          disabled={deletingId !== '' || (webLinksState.editingId === webLink.id && webLinksState.isEditing)}
          icon={getDeleteButtonIcon()}
          onClick={() => {
            webLinksDispatch({
              type: 'SET_WEB_LINK',
              payload: { webLink }
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
              id="addWebLinkButton"
              className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink dataflowHelp-weblink-upload-help-step`}
              icon="add"
              label={resources.messages['add']}
              onClick={() => setIsAddOrEditWebLinkDialogVisible(true)}
              style={{ float: 'left' }}
            />
          </div>
        </Toolbar>
      ) : (
        <></>
      )}
      <DataTable
        autoLayout={true}
        onSort={e => {
          setSortFieldWebLinks(e.sortField);
          setSortOrderWebLinks(e.sortOrder);
        }}
        paginator={false}
        selectionMode="single"
        sortField={sortFieldWebLinks}
        sortOrder={sortOrderWebLinks}
        value={webLinks}>
        {!isEmpty(webLinks) ? webLinksState.webLinksColumns : emptyWebLinkColumns}
      </DataTable>

      {isLoading && isEmpty(webLinks) && <Spinner style={{ top: 0 }} />}

      {!isLoading && isEmpty(webLinks) && (
        <div className={styles.noDataWrapper}>
          <h4>{resources.messages['noWebLinks']}</h4>
        </div>
      )}

      {webLinksState.isAddOrEditWebLinkDialogVisible && (
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
          visible={webLinksState.isAddOrEditWebLinkDialogVisible}>
          <form>
            <fieldset>
              <div className={`formField ${webLinksState.errors.description.hasErrors ? 'error' : ''}`}>
                <input
                  id={`descriptionWebLinks`}
                  ref={inputRef}
                  maxLength={255}
                  onChange={e => {
                    onDescriptionChange(e.target.value);
                  }}
                  onBlur={() => checkIsCorrectInputValue('description')}
                  onFocus={() => setErrors('description', { message: '', hasErrors: false })}
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

              <div className={`formField ${webLinksState.errors.url.hasErrors ? 'error' : ''}`}>
                <input
                  id={`urlWebLinks`}
                  name="url"
                  onChange={e => onWebLinkUrlChange(e.target.value)}
                  onBlur={() => checkIsCorrectInputValue('url')}
                  onFocus={() => setErrors('url', { message: '', hasErrors: false })}
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
                  id="submitButton"
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
          onConfirm={() => onDeleteWebLink(webLinksState.webLink.id)}
          onHide={onHideDeleteDialog}
          visible={webLinksState.isConfirmDeleteVisible}>
          {resources.messages['deleteWebLink']}
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
