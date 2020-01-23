import React, { useEffect, useContext, useReducer } from 'react';

import { isEmpty, isNull } from 'lodash';

import styles from './Category.module.css';

import { Button } from 'ui/views/_components/Button';
import { Codelist } from './_components/Codelist';
import { CodelistProperties } from 'ui/views/_components/CodelistProperties';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Dialog } from 'ui/views/_components/Dialog';
import { InputText } from 'ui/views/_components/InputText';
import { Spinner } from 'ui/views/_components/Spinner';
import { TreeViewExpandableItem } from 'ui/views/_components/TreeView/_components/TreeViewExpandableItem';

import { CodelistCategoryService } from 'core/services/CodelistCategory';
import { CodelistService } from 'core/services/Codelist';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { categoryReducer } from './_functions/Reducers/categoryReducer';

const Category = ({
  categoriesDropdown,
  category,
  checkDuplicates,
  isDataCustodian,
  isInDesign,
  onCodelistError,
  onCodelistSelected,
  onLoadCategories
}) => {
  const initialCategoryState = {
    categoryId: null,
    categoryDescription: '',
    categoryShortCode: '',
    filteredCodelists: [],
    isAddCodelistDialogVisible: '',
    isDeleteConfirmDialogVisible: false,
    isEditingDialogVisible: false,
    isExpanded: false,
    isFiltered: true,
    isLoading: false,
    codelists: [],
    codelistsInEdition: 0,
    codelistName: '',
    codelistVersion: '',
    codelistStatus: { statusType: 'design', value: 'DESIGN' },
    codelistDescription: ''
  };
  const [categoryState, dispatchCategory] = useReducer(categoryReducer, initialCategoryState);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  useEffect(() => {
    setCategoryInputs(category.description, category.shortCode, category.id);
  }, [onLoadCategories]);

  useEffect(() => {
    if (!isNull(categoryState.categoryId)) {
      onLoadCodelists();
    }
  }, [categoryState.isFiltered]);

  useEffect(() => {
    if (categoryState.isEditingDialogVisible) {
      onLoadCategoryInfo();
    }
  }, [categoryState.isEditingDialogVisible]);

  const onConfirmDeleteCategory = async () => {
    try {
      const response = await CodelistCategoryService.deleteById(categoryState.categoryId);
      onRefreshCategories(response);
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
    dispatchCategory({ type: 'EDIT_NEW_CODELIST', payload: { property, value } });
  };

  const onKeyChange = (event, property) => {
    if (event.key === 'Escape') {
      dispatchCategory({
        type: 'EDIT_NEW_CODELIST',
        payload: { property, value: initialCategoryState[property] }
      });
    } else if (event.key == 'Enter') {
    }
  };

  const onLoadCategoryInfo = async () => {
    const response = await CodelistCategoryService.getCategoryInfo(categoryState.categoryId);
    if (response.status >= 200 && response.status <= 299) {
      setCategoryInputs(response.data.description, response.data.shortCode, response.data.id);
    }
  };

  const onLoadCodelists = async () => {
    toggleLoading(true);
    try {
      const response = await CodelistService.getAllInCategory(categoryState.categoryId);
      if (categoryState.isFiltered) {
        dispatchCategory({
          type: 'SET_CODELISTS_IN_CATEGORY',
          payload: { data: response.filter(codelist => codelist.status.toUpperCase() !== 'DEPRECATED') }
        });
      } else {
        dispatchCategory({ type: 'SET_CODELISTS_IN_CATEGORY', payload: { data: response } });
      }
    } catch (error) {
    } finally {
      toggleLoading(false);
      toggleIsExpanded(true);
    }

    // setCategoryInputs(response.data.description, response.data.shortCode, response.data.id);
  };

  const onRefreshCategories = response => {
    if (response.status >= 200 && response.status <= 299) {
      onLoadCategories();
    }
  };

  const onRefreshCodelist = (codelistId, newCodelist) => {
    const inmCodelists = [...categoryState.codelists];
    dispatchCategory({
      type: 'SET_CODELISTS_IN_CATEGORY',
      payload: { data: inmCodelists.map(codelist => (newCodelist.id === codelist.id ? newCodelist : codelist)) }
    });
  };

  // const onRefreshCategory = response => {
  //   if (response.status >= 200 && response.status <= 299) {
  //     onLoadCategory();
  //   }
  // };

  const onSaveCategory = async () => {
    try {
      const response = await CodelistCategoryService.updateById(
        categoryState.categoryId,
        categoryState.categoryShortCode,
        categoryState.categoryDescription
      );
      onRefreshCategories(response);
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

  const onSaveCodelist = async () => {
    try {
      const response = await CodelistService.addById(
        categoryState.codelistDescription,
        [],
        categoryState.codelistName,
        'DESIGN',
        categoryState.codelistVersion,
        categoryState.categoryId
      );
      if (response.status >= 200 && response.status <= 299) {
        onLoadCategories();
      }
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

  const onShowDeprecatedCodelists = () => {
    dispatchCategory({ type: 'TOGGLE_FILTER_DEPRECATED_CODELISTS' });
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

  const checkNoCodelistEditing = () => categoryState.codelistsInEdition === 0;

  const editCategoryForm = (
    <React.Fragment>
      <span className={`${styles.categoryInput} p-float-label`}>
        <InputText
          id={'shortCodeInput'}
          onChange={e => setCategoryInputs(undefined, e.target.value)}
          value={categoryState.categoryShortCode}
        />
        <label htmlFor={'shortCodeInput'}>{resources.messages['categoryShortCode']}</label>
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

  const setCategoryInputs = (description, shortCode, id) =>
    dispatchCategory({
      type: 'SET_CATEGORY_INPUTS',
      payload: { description, shortCode, id }
    });

  const toggleDialog = (action, isVisible) =>
    dispatchCategory({
      type: action,
      payload: isVisible
    });

  const toggleIsExpanded = expanded => dispatchCategory({ type: 'TOGGLE_IS_EXPANDED', payload: expanded });

  const toggleLoading = loading => {
    dispatchCategory({ type: 'SET_ISLOADING', payload: { loading } });
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
        {!isEmpty(categoryState.codelists) && !categoryState.isLoading ? (
          categoryState.codelists.map((codelist, i) => {
            return (
              <Codelist
                categoriesDropdown={categoriesDropdown}
                categoryId={categoryState.categoryId}
                checkDuplicates={checkDuplicates}
                checkNoCodelistEditing={checkNoCodelistEditing}
                codelist={codelist}
                isDataCustodian={isDataCustodian}
                isInDesign={isInDesign}
                key={i}
                onCodelistError={onCodelistError}
                onCodelistSelected={onCodelistSelected}
                onRefreshCodelist={onRefreshCodelist}
                updateEditingCodelists={updateEditingCodelists}
              />
            );
          })
        ) : (
          <div className={styles.noCodelistsMessage}>
            <span>{resources.messages['noCodelists']}</span>
          </div>
        )}
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
        header={resources.messages['editCategory']}
        modal={true}
        onHide={() => toggleDialog('TOGGLE_EDIT_DIALOG_VISIBLE', false)}
        style={{ width: '50%' }}
        visible={categoryState.isEditingDialogVisible}>
        <div className="p-grid p-fluid"> {editCategoryForm}</div>
      </Dialog>
    ) : null;
  };

  const updateEditingCodelists = isNewEditingCodelist => {
    if (isNewEditingCodelist) {
      dispatchCategory({ type: 'UPDATE_EDITING_CODELISTS', payload: 1 });
    } else {
      dispatchCategory({ type: 'UPDATE_EDITING_CODELISTS', payload: -1 });
    }
  };

  return (
    <React.Fragment>
      <TreeViewExpandableItem
        className={styles.categoryExpandable}
        expanded={false}
        items={[{ label: category.shortCode }, { label: category.description }]}
        buttons={[
          {
            disabled: !categoryState.isExpanded || categoryState.codelists.length === 0,
            icon: 'filter',
            iconSlashed: categoryState.isFiltered,
            label: '',
            onClick: () => onShowDeprecatedCodelists(),
            tooltip: categoryState.isFiltered
              ? resources.messages['showDeprecatedCodelists']
              : resources.messages['hideDeprecatedCodelists']
          },
          {
            disabled: !checkNoCodelistEditing(),
            icon: 'pencil',
            label: '',
            onClick: () => toggleDialog('TOGGLE_EDIT_DIALOG_VISIBLE', true),
            tooltip: resources.messages['editCategory']
          },
          {
            disabled: category.codelistNumber > 0,
            icon: 'trash',
            label: '',
            onClick: () => toggleDialog('TOGGLE_DELETE_DIALOG_VISIBLE', true),
            tooltip: resources.messages['deleteCategory']
          },
          {
            disabled: !checkNoCodelistEditing(),
            icon: 'add',
            label: '',
            onClick: () => toggleDialog('TOGGLE_ADD_CODELIST_DIALOG_VISIBLE', true),
            tooltip: resources.messages['newCodelist']
          }
        ]}
        onCollapseTree={() => toggleIsExpanded(false)}
        onExpandTree={onLoadCodelists}>
        {// <React.Fragment>
        //   <div className={styles.codelistHeader}>
        //     <span>Name</span>
        //     <span>Version</span>
        //     <span>Status</span>
        //     <span>Description</span>
        //   </div>
        categoryState.isLoading ? <Spinner className={styles.positioning} /> : renderCodelist()
        // </React.Fragment>
        }
      </TreeViewExpandableItem>
      {renderEditDialog()}
      {renderAddCodelistDialog()}
      {renderDeleteDialog()}
    </React.Fragment>
  );
};

export { Category };
