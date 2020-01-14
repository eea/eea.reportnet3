import React, { useEffect, useContext, useReducer } from 'react';

import { isNull } from 'lodash';

import styles from './Category.module.css';

import { Button } from 'ui/views/_components/Button';
import { Codelist } from './_components/Codelist';
import { CodelistProperties } from 'ui/views/_components/CodelistProperties';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Dialog } from 'ui/views/_components/Dialog';
import { InputText } from 'ui/views/_components/InputText';
import { TreeViewExpandableItem } from 'ui/views/_components/TreeView/_components/TreeViewExpandableItem';

import { CodelistCategoryService } from 'core/services/CodelistCategory';
import { CodelistService } from 'core/services/Codelist';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { categoryReducer } from './_functions/Reducers/categoryReducer';

const Category = ({ category, checkDuplicates, isDataCustodian, isInDesign, onCodelistSelected }) => {
  const initialCategoryState = {
    categoryId: null,
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
      await CodelistService.deleteById(categoryState.categoryId);
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
    try {
      if (!isNull(categoryState.categoryId)) {
        //CodelistCategoryService.addById(categoryState.categoryName, categoryState.categoryDescription);
      } else {
        CodelistCategoryService.updateById(
          categoryState.categoryId,
          categoryState.categoryName,
          categoryState.categoryDescription
        );
      }
    } catch (error) {
      notificationContext.add({
        type: 'ADD_CODELIST_CATEGORY_BY_ID_ERROR',
        content: {
          // dataflowId,
          // datasetId
        }
      });
    } finally {
      toggleDialog('TOGGLE_EDIT_DIALOG_VISIBLE', false);
    }
  };

  const onSaveCodelist = () => {
    try {
      // CodelistService.addById(
      //   dataflowId,
      //   categoryState.codelistDescription,
      //   [],
      //   categoryState.codelistName,
      //   categoryState.codelistStatus,
      //   categoryState.codelistVersion
      // );
    } catch (error) {
      notificationContext.add({
        type: 'ADD_CODELIST_BY_ID_ERROR',
        content: {
          // dataflowId,
          // datasetId
        }
      });
    } finally {
      toggleDialog('TOGGLE_EDIT_DIALOG_VISIBLE', false);
    }
  };

  const addCodelistDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button label={resources.messages['save']} icon="save" onClick={onSaveCodelist} />
      <Button
        label={resources.messages['cancel']}
        icon="cancel"
        onClick={() => toggleDialog('TOGGLE_ADD_CODELIST_DIALOG_VISIBLE', false)}
      />
    </div>
  );

  const addCodelistForm = (
    <CodelistProperties
      checkDuplicates={checkDuplicates}
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

  const setCategoryInputs = (description, name, id) => {
    dispatchCategory({
      type: 'SET_CATEGORY_INPUTS',
      payload: { description, name, id }
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

  const renderCodelist = () => {
    return (
      <div className={styles.categories}>
        {category.codelists.map((codelist, i) => {
          return (
            <Codelist
              checkDuplicates={checkDuplicates}
              codelist={codelist}
              isDataCustodian={isDataCustodian}
              isInDesign={isInDesign}
              key={i}
              onCodelistSelected={onCodelistSelected}
            />
          );
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
      <TreeViewExpandableItem
        className={styles.categoryExpandable}
        expanded={true}
        items={[category.name, category.description]}
        buttons={[
          {
            label: '',
            tooltip: resources.messages['editCategory'],
            icon: 'pencil',
            onClick: () => toggleDialog('TOGGLE_EDIT_DIALOG_VISIBLE', true)
          },
          {
            label: '',
            disabled: category.codelists.length > 0,
            tooltip: resources.messages['deleteCategory'],
            icon: 'trash',
            onClick: () => toggleDialog('TOGGLE_DELETE_DIALOG_VISIBLE', true)
          },
          {
            label: '',
            tooltip: resources.messages['newCodelist'],
            icon: 'add',
            onClick: () => toggleDialog('TOGGLE_ADD_CODELIST_DIALOG_VISIBLE', true)
          }
        ]}>
        {renderCodelist()}
      </TreeViewExpandableItem>
      {renderEditDialog()}
      {renderAddCodelistDialog()}
      {renderDeleteDialog()}
    </React.Fragment>
  );
};

export { Category };
