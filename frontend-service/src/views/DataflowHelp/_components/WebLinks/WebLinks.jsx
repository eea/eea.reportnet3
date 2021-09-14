import { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './WebLinks.module.scss';

import { config } from 'conf';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { DataTable } from 'views/_components/DataTable';
import { Dialog } from 'views/_components/Dialog';
import { ErrorMessage } from 'views/_components/ErrorMessage';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { Spinner } from 'views/_components/Spinner';
import { Toolbar } from 'views/_components/Toolbar';

import { RegularExpressions } from 'views/_functions/Utils/RegularExpressions';
import { WebLinkService } from 'services/WebLinkService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
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
  const resourcesContext = useContext(ResourcesContext);

  const inputRef = useRef(null);

  const [webLinksState, webLinksDispatch] = useReducer(webLinksReducer, {
    editingId: null,
    errors: { description: { message: '', hasErrors: false }, url: { message: '', hasErrors: false } },
    isAddOrEditWebLinkDialogVisible: false,
    isConfirmDeleteVisible: false,
    isDeleting: false,
    isSubmitting: false,
    webLink: { id: undefined, isPublic: false, description: '', url: '' },
    webLinksColumns: []
  });

  const [deletingId, setDeletingId] = useState('');

  useEffect(() => {
    if (!isNil(inputRef.current)) inputRef.current.focus();
  }, [inputRef, webLinksState.isAddOrEditWebLinkDialogVisible]);

  const getOrderedWeblinkColumns = webLinks => {
    const representativesWithPriority = [
      { id: 'description', index: 0 },
      { id: 'url', index: 1 },
      { id: 'isPublic', index: 2 }
    ];

    return webLinks
      .map(field => representativesWithPriority.filter(e => field === e.id))
      .flat()
      .sort((a, b) => a.index - b.index)
      .map(orderedField => orderedField.id);
  };

  useEffect(() => {
    let webLinkKeys = !isEmpty(webLinks) ? getOrderedWeblinkColumns(Object.keys(webLinks[0])) : [];
    let webLinkColArray = webLinkKeys
      .filter(key => key !== 'id')
      .map(key => {
        let template = null;
        if (key === 'url') template = linkTemplate;
        if (key === 'isPublic') template = isPublicColumnTemplate;
        return (
          <Column
            body={template}
            columnResizeMode="expand"
            field={key}
            filter={false}
            filterMatchMode="contains"
            header={getHeader(key)}
            key={key}
            sortable={true}
          />
        );
      });

    if (isToolbarVisible) webLinkColArray = [...webLinkColArray, webLinkEditionColumn];

    webLinksDispatch({ type: 'SET_WEB_LINKS_COLUMNS', payload: { webLinksColumns: webLinkColArray } });
  }, [webLinks, webLinksState.webLink, isToolbarVisible, isLoading]);

  const checkIsValidUrl = url => RegularExpressions['url'].test(url);

  const checkIsCorrectLength = inputValue => inputValue.length <= config.INPUT_MAX_LENGTH;

  const checkIsEmptyInput = inputValue => inputValue.trim() === '';

  const checkIsCorrectInputValue = inputName => {
    let hasErrors = false;
    let message = '';
    const inputValue = webLinksState.webLink[inputName];

    if (checkIsEmptyInput(inputValue)) {
      message = '';
      hasErrors = true;
    } else if (inputName === 'description' && !checkIsCorrectLength(inputValue)) {
      message = resourcesContext.messages['webLinkDescriptionValidationMax'];
      hasErrors = true;
    } else if (inputName === 'url' && !checkIsValidUrl(inputValue)) {
      message = resourcesContext.messages['urlError'];
      hasErrors = true;
    }

    setErrors(inputName, { message, hasErrors });

    return hasErrors;
  };

  const getHeader = fieldHeader => {
    switch (fieldHeader) {
      default:
        return resourcesContext.messages[fieldHeader];
    }
  };

  const isPublicColumnTemplate = rowData => (
    <div className={styles.iconStyle}>
      <span>
        {rowData.isPublic ? (
          <FontAwesomeIcon aria-label={resourcesContext.messages['isPublic']} icon={AwesomeIcons('check')} />
        ) : (
          ''
        )}
      </span>
    </div>
  );

  const fieldsArray = [
    { field: 'description', header: resourcesContext.messages['description'] },
    { field: 'isPublic', header: resourcesContext.messages['isPublic'] },
    { field: 'url', header: resourcesContext.messages['url'] }
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
    <a href={getValidUrl(rowData.url)} rel="noopener noreferrer" target="_blank">
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
      await WebLinkService.delete(id);
      onLoadWebLinks();
    } catch (error) {
      console.error('WebLinks - onDeleteWebLink.', error);
      notificationContext.add({ type: 'DELETE_WEB_LINK_ERROR' });
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

  const onUrlChange = inputValue => webLinksDispatch({ type: 'ON_URL_CHANGE', payload: { url: inputValue } });

  const onIsPublicChange = inputValue =>
    webLinksDispatch({ type: 'ON_IS_PUBLIC_CHANGE', payload: { isPublic: inputValue } });

  const onHideDeleteDialog = () => webLinksDispatch({ type: 'ON_HIDE_DELETE_DIALOG' });

  const setIsConfirmDeleteVisible = isVisible =>
    webLinksDispatch({
      type: 'SET_IS_CONFIRM_DELETE_VISIBLE',
      payload: { isConfirmDeleteVisible: isVisible }
    });

  const onSaveRecord = async () => {
    checkIsCorrectInputValue('description');
    checkIsCorrectInputValue('url');

    if (!webLinksState.errors.url.hasErrors && !webLinksState.errors.description.hasErrors) {
      webLinksDispatch({ type: 'ON_SAVE_RECORD', payload: { webLink: webLinksState.webLink } });
      if (isNil(webLinksState.webLink.id)) {
        try {
          await WebLinkService.create(dataflowId, webLinksState.webLink);
          onLoadWebLinks();
          onHideAddEditDialog();
        } catch (error) {
          console.error('WebLinks - onSaveRecord - add.', error);
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
          await WebLinkService.update(dataflowId, webLinksState.webLink);
          onLoadWebLinks();
          onHideAddEditDialog();
        } catch (error) {
          console.error('WebLinks - onSaveRecord - update.', error);
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
    <Column body={row => webLinkEditButtons(row)} key={'buttonsUniqueId'} style={{ width: '5em' }} />
  );

  return (
    <Fragment>
      {isToolbarVisible && (
        <Toolbar className={styles.webLinksToolbar}>
          <div className="p-toolbar-group-left">
            <Button
              className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink dataflowHelp-webLink-upload-help-step`}
              icon="add"
              id="addWebLinkButton"
              label={resourcesContext.messages['add']}
              onClick={() => setIsAddOrEditWebLinkDialogVisible(true)}
              style={{ float: 'left' }}
            />
          </div>
        </Toolbar>
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
          <h4>{resourcesContext.messages['noWebLinks']}</h4>
        </div>
      )}

      {webLinksState.isAddOrEditWebLinkDialogVisible && (
        <Dialog
          blockScroll={false}
          className={styles.dialog}
          contentStyle={{ height: '80%', maxHeight: '80%', overflow: 'auto' }}
          header={
            isNil(webLinksState.webLink.id)
              ? resourcesContext.messages['createNewWebLink']
              : resourcesContext.messages['editWebLink']
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
                  maxLength={config.INPUT_MAX_LENGTH}
                  name="description"
                  onBlur={() => checkIsCorrectInputValue('description')}
                  onChange={e => {
                    onDescriptionChange(e.target.value);
                  }}
                  onFocus={() => setErrors('description', { message: '', hasErrors: false })}
                  onKeyPress={e => {
                    if (e.key === 'Enter' && !checkIsCorrectInputValue('description')) {
                      onSaveRecord();
                    }
                  }}
                  placeholder={resourcesContext.messages['description']}
                  ref={inputRef}
                  type="text"
                  value={webLinksState.webLink.description}
                />
                <label className="srOnly" htmlFor="descriptionWebLinks">
                  {resourcesContext.messages['description']}
                </label>
                {webLinksState.errors.description.message !== '' && (
                  <ErrorMessage message={webLinksState.errors.description.message} />
                )}
              </div>

              <div className={`formField ${webLinksState.errors.url.hasErrors ? 'error' : ''}`}>
                <input
                  id={`urlWebLinks`}
                  name="url"
                  onBlur={() => checkIsCorrectInputValue('url')}
                  onChange={e => onUrlChange(e.target.value)}
                  onFocus={() => setErrors('url', { message: '', hasErrors: false })}
                  onKeyPress={e => {
                    if (e.key === 'Enter' && !checkIsCorrectInputValue('url')) {
                      onSaveRecord();
                    }
                  }}
                  placeholder={resourcesContext.messages['url']}
                  type="text"
                  value={webLinksState.webLink.url}
                />
                <label className="srOnly" htmlFor="urlWebLinks">
                  {resourcesContext.messages['url']}
                </label>
                {webLinksState.errors.url.message !== '' && <ErrorMessage message={webLinksState.errors.url.message} />}
              </div>
            </fieldset>

            <fieldset>
              <div className={styles.checkboxIsPublic}>
                <input
                  checked={webLinksState.webLink.isPublic}
                  id="isPublic"
                  onChange={() => onIsPublicChange(!webLinksState.webLink.isPublic)}
                  type="checkbox"
                />
                <label htmlFor="isPublic" style={{ display: 'block' }}>
                  {resourcesContext.messages['checkboxIsPublic']}
                </label>
              </div>
            </fieldset>

            <fieldset>
              <div className={`${styles.buttonWrap} ui-dialog-buttonpane p-clearfix`}>
                <Button
                  disabled={webLinksState.isSubmitting}
                  icon={getButtonIcon(webLinksState.isSubmitting)}
                  id="submitButton"
                  label={
                    isNil(webLinksState.webLink.id)
                      ? resourcesContext.messages['add']
                      : resourcesContext.messages['edit']
                  }
                  onClick={() => onSaveRecord()}
                />
                <Button
                  className={`${styles.cancelButton} p-button-secondary button-right-aligned`}
                  icon="cancel"
                  label={resourcesContext.messages['cancel']}
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
          header={resourcesContext.messages['delete']}
          iconConfirm={webLinksState.isDeleting ? 'spinnerAnimate' : 'check'}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={() => onDeleteWebLink(webLinksState.webLink.id)}
          onHide={onHideDeleteDialog}
          visible={webLinksState.isConfirmDeleteVisible}>
          {resourcesContext.messages['deleteWebLink']}
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
