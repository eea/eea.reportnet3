import React, { useEffect, useContext, useReducer } from 'react';

import styles from './Category.module.css';

import { Button } from 'ui/views/_components/Button';
import { Codelist } from './_components/Codelist';
import { CodelistProperties } from 'ui/views/_components/CodelistProperties';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Dialog } from 'ui/views/_components/Dialog';
import { InputText } from 'ui/views/_components/InputText';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { categoryReducer } from './_functions/Reducers/categoryReducer';

const Category = ({ category, isDataCustodian }) => {
  const initialCategoryState = {
    categoryDescription: '',
    categoryName: '',
    isAddCodelistDialogVisible: '',
    isDeleteConfirmDialogVisible: false,
    isEditingDialogVisible: false,
    codelistName: '',
    codelistVersion: '',
    codelistStatus: { statusType: 'design', value: 'DESIGN' },
    codelistDescription: ''
  };
  const [categoryState, dispatchCategory] = useReducer(categoryReducer, initialCategoryState);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  useEffect(() => {
    if (categoryState.isEditingDialogVisible) {
      setCategoryInputs(category.description, category.name);
    }
  }, [categoryState.isEditingDialogVisible]);

  const onConfirmDeleteCategory = async () => {
    try {
      //await DatasetService.deleteRecordById(datasetId, records.selectedRecord.recordId);
    } catch (error) {
      notificationContext.add({
        type: 'DELETE_CODELIST_CATEGORY_BY_ID_ERROR',
        content: {
          // dataflowId,
          // datasetId
        }
      });
    } finally {
      toggleDialog('TOGGLE_DELETE_DIALOG_VISIBLE', false);
    }
  };

  const onEditorPropertiesInputChange = (value, property) => {
    console.log({ value, property });
    dispatchCategory({ type: 'EDIT_NEW_CODELIST', payload: { property, value } });
  };

  const onKeyChange = (event, property) => {
    if (event.key === 'Escape') {
      console.log(initialCategoryState[property], initialCategoryState, property);
      dispatchCategory({
        type: 'EDIT_NEW_CODELIST',
        payload: { property, value: initialCategoryState[property] }
      });
    } else if (event.key == 'Enter') {
    }
  };

  const onSaveCategory = () => {
    //API CALL
    try {
    } catch (error) {
    } finally {
      toggleDialog('TOGGLE_EDIT_DIALOG_VISIBLE', false);
    }
  };

  const onSaveCodelist = () => {
    //API CALL
    try {
    } catch (error) {
    } finally {
      toggleDialog('TOGGLE_EDIT_DIALOG_VISIBLE', false);
    }
  };

  const addCodelistDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button label={resources.messages['save']} icon="save" onClick={() => onSaveCodelist()} />
      <Button
        label={resources.messages['cancel']}
        icon="cancel"
        onClick={() => toggleDialog('TOGGLE_ADD_CODELIST_DIALOG_VISIBLE', false)}
      />
    </div>
  );

  const addCodelistForm = (
    <CodelistProperties
      isEmbedded={true}
      onEditorPropertiesInputChange={onEditorPropertiesInputChange}
      onKeyChange={onKeyChange}
      state={categoryState}
    />
  );

  const categoryDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button label={resources.messages['save']} icon="save" onClick={() => onSaveCategory()} />
      <Button
        label={resources.messages['cancel']}
        icon="cancel"
        onClick={() => toggleDialog('TOGGLE_EDIT_DIALOG_VISIBLE', false)}
      />
    </div>
  );

  const editCategoryForm = (
    <React.Fragment>
      <span className={`${styles.categoryInput} p-float-label`}>
        <InputText
          id={'nameInput'}
          onChange={e => setCategoryInputs(undefined, e.target.value)}
          value={categoryState.categoryName}
        />
        <label htmlFor={'nameInput'}>{resources.messages['categoryName']}</label>
      </span>
      <span className={`${styles.categoryInput} p-float-label`}>
        <InputText
          id={'descriptionInput'}
          onChange={e => setCategoryInputs(e.target.value)}
          // required={true}
          value={categoryState.categoryDescription}
        />
        <label htmlFor={'descriptionInput'}>{resources.messages['categoryDescription']}</label>
      </span>
    </React.Fragment>
  );

  const setCategoryInputs = (description, name) => {
    dispatchCategory({
      type: 'SET_CATEGORY_INPUTS',
      payload: { description, name }
    });
  };

  const toggleDialog = (action, isVisible) => {
    dispatchCategory({
      type: action,
      payload: isVisible
    });
  };

  const renderDeleteDialog = () => {
    return categoryState.isDeleteConfirmDialogVisible ? (
      <ConfirmDialog
        onConfirm={onConfirmDeleteCategory}
        onHide={() => toggleDialog('TOGGLE_DELETE_DIALOG_VISIBLE', false)}
        visible={categoryState.isDeleteConfirmDialogVisible}
        header={resources.messages['deleteCategory']}
        labelConfirm={resources.messages['yes']}
        labelCancel={resources.messages['no']}>
        {resources.messages['confirmDeleteCategory']}
      </ConfirmDialog>
    ) : null;
  };

  const renderEditButton = () => {
    return isDataCustodian ? (
      <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
        <Button
          label={resources.messages['editCategory']}
          icon="pencil"
          onClick={() => {
            toggleDialog('TOGGLE_EDIT_DIALOG_VISIBLE', true);
          }}
        />
        <Button
          label={resources.messages['deleteCategory']}
          disabled={category.codelists.length > 0}
          icon="trash"
          onClick={() => {
            toggleDialog('TOGGLE_DELETE_DIALOG_VISIBLE', true);
          }}
          style={{ marginLeft: '0.5rem' }}
        />
        <Button
          label={resources.messages['newCodelist']}
          icon="add"
          onClick={() => {
            toggleDialog('TOGGLE_ADD_CODELIST_DIALOG_VISIBLE', true);
          }}
          style={{ marginLeft: '0.5rem' }}
        />
      </div>
    ) : null;
  };

  const renderCodelist = () => {
    return (
      <div className={styles.categories}>
        {category.codelists.map(codelist => {
          return <Codelist codelist={codelist} />;
        })}
      </div>
    );
  };

  const renderAddCodelistDialog = () => {
    return categoryState.isAddCodelistDialogVisible ? (
      <Dialog
        className="edit-table"
        blockScroll={false}
        contentStyle={{ height: '80%', maxHeight: '80%', overflow: 'auto' }}
        closeOnEscape={false}
        footer={addCodelistDialogFooter}
        header={resources.messages['addNewCodelist']}
        modal={true}
        onHide={() => toggleDialog('TOGGLE_ADD_CODELIST_DIALOG_VISIBLE', false)}
        style={{ width: '60%' }}
        visible={categoryState.isAddCodelistDialogVisible}>
        <div className="p-grid p-fluid">{addCodelistForm}</div>
      </Dialog>
    ) : null;
  };

  const renderEditDialog = () => {
    return categoryState.isEditingDialogVisible ? (
      <Dialog
        className="edit-table"
        blockScroll={false}
        contentStyle={{ height: '80%', maxHeight: '80%', overflow: 'auto' }}
        closeOnEscape={false}
        footer={categoryDialogFooter}
        header={resources.messages['addNewCategory']}
        modal={true}
        onHide={() => toggleDialog('TOGGLE_EDIT_DIALOG_VISIBLE', false)}
        style={{ width: '50%' }}
        visible={categoryState.isEditingDialogVisible}>
        <div className="p-grid p-fluid"> {editCategoryForm}</div>
      </Dialog>
    ) : null;
  };

  return (
    <React.Fragment>
      {renderEditButton()}
      {renderCodelist()}
      {renderEditDialog()}
      {renderAddCodelistDialog()}
      {renderDeleteDialog()}
    </React.Fragment>
  );
};

export { Category };
