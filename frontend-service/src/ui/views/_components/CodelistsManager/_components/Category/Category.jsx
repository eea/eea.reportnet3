import React, { useEffect, useContext, useReducer } from 'react';

import { isEmpty, isNull, isUndefined, cloneDeep } from 'lodash';

import styles from './Category.module.css';

import { Button } from 'ui/views/_components/Button';
import { Codelist } from './_components/Codelist';
import { CodelistProperties } from 'ui/views/_components/CodelistProperties';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Dialog } from 'ui/views/_components/Dialog';
import { InputText } from 'ui/views/_components/InputText';
import { MultiSelect } from 'primereact/multiselect';
import { TreeViewExpandableItem } from 'ui/views/_components/TreeView/_components/TreeViewExpandableItem';

import { CodelistCategoryService } from 'core/services/CodelistCategory';
import { CodelistService } from 'core/services/Codelist';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { categoryReducer } from './_functions/Reducers/categoryReducer';

import { CategoryUtils } from './_functions/Utils/CategoryUtils';

const Category = ({
  categoriesDropdown,
  category,
  checkCategoryDuplicates,
  checkDuplicates,
  collapseAll,
  expandAll,
  expandedStatus,
  isDataCustodian,
  isEditionModeOn,
  isIncorrect,
  isInDesign,
  onCalculateExpandedStatus,
  onCodelistError,
  onCodelistSelected,
  onLoadCategories,
  onRefreshCategory,
  // onLoadCategory,
  onToggleIncorrect,
  toggleExpandAll,
  updateEditingCodelists
}) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const initialCategoryState = {
    categoryId: null,
    categoryDescription: '',
    categoryShortCode: '',
    codelists: cloneDeep(category.codelists),
    codelistName: '',
    codelistDescription: '',
    codelistStatus: { statusType: 'design', value: 'DESIGN' },
    codelistsInEdition: 0,
    codelistVersion: '',
    expanded: false,
    filter: {
      name: '',
      version: '',
      status: !isInDesign
        ? [{ statusType: 'Design', value: 'design' }, { statusType: 'Ready', value: 'ready' }]
        : [{ statusType: 'Ready', value: 'ready' }],
      description: ''
    },
    filteredCodelists: cloneDeep(category.codelists),
    isAddCodelistDialogVisible: '',
    isDeleteConfirmDialogVisible: false,
    isEditingDialogVisible: false,
    isFiltered: true,
    isKeyFiltered: false,
    isSaving: false,
    order: { name: 1, version: 1, status: 1, description: 1 }
  };
  const [categoryState, dispatchCategory] = useReducer(categoryReducer, initialCategoryState);

  const statusTypes = [
    { statusType: 'Design', value: 'design' },
    { statusType: 'Ready', value: 'ready' },
    { statusType: 'Deprecated', value: 'deprecated' }
  ];

  useEffect(() => {
    if (!isIncorrect) {
      onLoadCodelists();
      // onRefreshCategory(category);
    }
  }, [category.codelists]);

  // useEffect(() => {
  //   onRefreshCategory(category);
  // }, []);

  useEffect(() => {
    console.log({ isEditionModeOn, isInDesign });
    if (!isUndefined(isEditionModeOn)) {
      if (isEditionModeOn) {
        changeFilterValues(
          'status',
          [{ statusType: 'Design', value: 'design' }, { statusType: 'Ready', value: 'ready' }],
          category.codelists
        );
      } else {
        if (isInDesign) {
          changeFilterValues('status', [{ statusType: 'Ready', value: 'ready' }], category.codelists);
        } else {
          changeFilterValues(
            'status',
            [{ statusType: 'Design', value: 'design' }, { statusType: 'Ready', value: 'ready' }],
            category.codelists
          );
        }
      }
    }
  }, [isEditionModeOn]);

  useEffect(() => {
    setCategoryInputs(category.description, category.shortCode, category.id);
  }, [onLoadCategories]);

  useEffect(() => {
    if (categoryState.isEditingDialogVisible) {
      onLoadCategoryInfo();
    }
  }, [categoryState.isEditingDialogVisible]);

  const onChangeExpandedStatus = expanded => {
    dispatchCategory({ type: 'TOGGLE_EXPANDED', payload: { expanded } });
  };

  const onConfirmDeleteCategory = async () => {
    try {
      const response = await CodelistCategoryService.deleteById(categoryState.categoryId);
      onRefreshCategories(response);
    } catch (error) {
      notificationContext.add({
        type: 'CODELIST_CATEGORY_SERVICE_DELETE_BY_ID_ERROR'
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

  const onLoadCategoryInfo = () => {
    setCategoryInputs(category.description, category.shortCode, category.id);
  };

  const onLoadCodelists = () => {
    changeFilterValues('status', categoryState.filter.status, category.codelists);
    dispatchCategory({
      type: 'SET_CODELISTS_IN_CATEGORY',
      payload: { data: category.codelists }
    });
  };

  const onOrderCodelists = (order, property) => {
    dispatchCategory({ type: 'ORDER_CODELISTS', payload: { order, property } });
  };

  const onRefreshCategories = response => {
    if (response.status >= 200 && response.status <= 299) {
      onLoadCategories();
    }
  };

  const onRefreshCodelist = (codelistId, newCodelist) => {
    const inmCodelists = [...categoryState.codelists];
    const updatedCodelists = inmCodelists.map(codelist => (newCodelist.id === codelist.id ? newCodelist : codelist));
    dispatchCategory({
      type: 'SET_CODELISTS_IN_CATEGORY',
      payload: { data: updatedCodelists }
    });
    onRefreshCategory(category, updatedCodelists);
    changeFilterValues('status', categoryState.filter.status, updatedCodelists);
  };

  const onSaveCategory = async () => {
    onToggleIncorrect(true);
    try {
      const response = await CodelistCategoryService.updateById(
        categoryState.categoryId,
        categoryState.categoryShortCode,
        categoryState.categoryDescription
      );
      onRefreshCategories(response);
    } catch (error) {
      notificationContext.add({
        type: 'CODELIST_CATEGORY_SERVICE_UPDATE_BY_ID_ERROR'
      });
    } finally {
      onToggleIncorrect(false);
      toggleDialog('TOGGLE_EDIT_DIALOG_VISIBLE', false);
    }
  };

  const onSaveCodelist = async () => {
    dispatchCategory({ type: 'TOGGLE_IS_SAVING', payload: true });
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
        type: 'CODELIST_SERVICE_ADD_BY_ID_ERROR'
      });
    } finally {
      dispatchCategory({ type: 'TOGGLE_IS_SAVING', payload: false });
      toggleDialog('TOGGLE_EDIT_DIALOG_VISIBLE', false);
    }
  };

  // const onShowDeprecatedCodelists = () => {
  //   dispatchCategory({ type: 'TOGGLE_FILTER_DEPRECATED_CODELISTS' });
  // };
  const addCodelistDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        className="p-button-success"
        disabled={
          isIncorrect ||
          categoryState.isSaving ||
          (categoryState.codelistName.trim() === '' || categoryState.codelistVersion.trim() === '')
        }
        icon="save"
        label={resources.messages['save']}
        onClick={onSaveCodelist}
      />
      <Button
        className="p-button-secondary-transparent"
        icon="cancel"
        label={resources.messages['cancel']}
        onClick={() => {
          toggleDialog('TOGGLE_ADD_CODELIST_DIALOG_VISIBLE', false);
        }}
      />
    </div>
  );

  const addCodelistForm = (
    <CodelistProperties
      checkDuplicates={checkDuplicates}
      isIncorrect={isIncorrect}
      onToggleIncorrect={onToggleIncorrect}
      onEditorPropertiesInputChange={onEditorPropertiesInputChange}
      onKeyChange={onKeyChange}
      state={categoryState}
    />
  );

  const categoryDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        className="p-button-success"
        disabled={
          isIncorrect || !(!isEmpty(categoryState.categoryShortCode) && !isEmpty(categoryState.categoryDescription))
        }
        icon="save"
        label={resources.messages['save']}
        onClick={() => onSaveCategory()}
      />
      <Button
        className="p-button-danger"
        icon="cancel"
        label={resources.messages['cancel']}
        onClick={() => {
          toggleDialog('TOGGLE_EDIT_DIALOG_VISIBLE', false);
          dispatchCategory({
            type: 'RESET_INITIAL_CATEGORY_VALUES',
            payload: category
          });
          onToggleIncorrect(false);
        }}
      />
    </div>
  );

  const changeFilterValues = (filter, value, data) => {
    dispatchCategory({
      type: 'SET_FILTER_VALUES',
      payload: { data, filter, value }
    });
  };

  const checkNoCodelistEditing = () => categoryState.codelistsInEdition === 0;

  const editCategoryForm = (
    <React.Fragment>
      <span className={`${styles.categoryEditInput} p-float-label`}>
        <InputText
          className={
            isIncorrect || categoryState.categoryShortCode.trim() === '' ? styles.categoryIncorrectInput : null
          }
          id={'shortCodeInput'}
          onBlur={() =>
            onToggleIncorrect(checkCategoryDuplicates(categoryState.categoryShortCode, categoryState.categoryId))
          }
          onChange={e => setCategoryInputs(undefined, e.target.value)}
          value={categoryState.categoryShortCode}
        />
        <label htmlFor={'shortCodeInput'}>{resources.messages['categoryShortCode']}</label>
      </span>
      <span className={`${styles.categoryEditInput} p-float-label`}>
        <InputText
          className={
            isIncorrect || categoryState.categoryDescription.trim() === '' ? styles.categoryIncorrectInput : null
          }
          id={'descriptionInput'}
          onChange={e => setCategoryInputs(e.target.value)}
          // required={true}
          value={categoryState.categoryDescription}
        />
        <label htmlFor={'descriptionInput'}>{resources.messages['categoryDescription']}</label>
      </span>
    </React.Fragment>
  );

  const getCodelists = (codelist, i) => {
    return (
      <Codelist
        categoriesDropdown={categoriesDropdown}
        categoryId={categoryState.categoryId}
        checkDuplicates={checkDuplicates}
        checkNoCodelistEditing={checkNoCodelistEditing}
        codelist={codelist}
        isDataCustodian={isDataCustodian}
        isEditionModeOn={isEditionModeOn}
        isIncorrect={isIncorrect}
        isInDesign={isInDesign}
        key={i}
        onCodelistError={onCodelistError}
        onCodelistSelected={onCodelistSelected}
        onLoadCategories={onLoadCategories}
        // onLoadCategory={onLoadCategory}
        onLoadCodelists={onLoadCodelists}
        onRefreshCodelist={onRefreshCodelist}
        onToggleIncorrect={onToggleIncorrect}
        updateEditingCodelists={updateEditingCodelists}
      />
    );
  };

  const getStatusStyle = status => {
    if (status.toLowerCase() === 'design') return styles.designBox;
    if (status.toLowerCase() === 'deprecated') return styles.deprecatedBox;
    if (status.toLowerCase() === 'ready') return styles.readyBox;
  };

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
      <React.Fragment>
        {renderFilters()}
        <div className={styles.categories}>
          {categoryState.filteredCodelists.map((codelist, i) => {
            return getCodelists(cloneDeep(codelist), i);
          })}
        </div>
      </React.Fragment>
    );
  };

  const renderAddCodelistDialog = () => {
    return categoryState.isAddCodelistDialogVisible ? (
      <Dialog
        className="edit-table"
        blockScroll={false}
        closeOnEscape={false}
        footer={addCodelistDialogFooter}
        header={resources.messages['addNewCodelist']}
        modal={true}
        onHide={() => toggleDialog('TOGGLE_ADD_CODELIST_DIALOG_VISIBLE', false)}
        style={{ width: '60%' }}
        visible={categoryState.isAddCodelistDialogVisible}
        zIndex={3003}>
        <div className="p-grid p-fluid">{addCodelistForm}</div>
      </Dialog>
    ) : null;
  };

  const renderEditDialog = () => {
    return categoryState.isEditingDialogVisible ? (
      <Dialog
        className="edit-table"
        blockScroll={false}
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

  const renderFilterOrder = property => {
    return (
      <Button
        className={`p-button-secondary-transparent ${styles.orderIcon}`}
        icon={categoryState.order[property] === 1 ? 'alphabeticOrderUp' : 'alphabeticOrderDown'}
        onClick={() => onOrderCodelists(categoryState.order[property], property)}
        style={{ fontSize: '12pt' }}
        tooltip={resources.messages['orderAlphabetically']}
        tooltipOptions={{ position: 'bottom' }}
      />
    );
  };

  const renderFilters = () => {
    return (
      <div className={styles.codelistHeader}>
        <span className={`${styles.categoryInput} p-float-label`}>
          <InputText
            className={styles.inputFilter}
            id={'filterNameInput'}
            onChange={e => changeFilterValues('name', e.target.value, categoryState.codelists)}
            value={categoryState.filter.name}
          />
          <label htmlFor={'filterNameInput'}>{resources.messages['codelistName']}</label>
        </span>
        {renderFilterOrder('name')}
        <span className={`${styles.categoryInput} p-float-label`}>
          <InputText
            className={styles.inputFilter}
            id={'filterVersionInput'}
            onChange={e => changeFilterValues('version', e.target.value, categoryState.codelists)}
            value={categoryState.filter.version}
          />
          <label htmlFor={'filterVersionInput'}>{resources.messages['codelistVersion']}</label>
        </span>
        {renderFilterOrder('version')}
        <span className={`${styles.categoryInput}`}>
          <MultiSelect
            className={styles.multiselectFilter}
            filter={false}
            itemTemplate={statusTemplate}
            onChange={e => changeFilterValues('status', e.value, categoryState.codelists)}
            optionLabel="statusType"
            options={statusTypes}
            placeholder={resources.messages['codelistStatus']}
            style={{ fontSize: '10pt', color: 'var(--floating-label-color)' }}
            value={categoryState.filter.status}
          />
        </span>
        {renderFilterOrder('status')}
        <span className={`${styles.categoryInput} p-float-label`}>
          <InputText
            className={styles.inputFilter}
            id={'filterDescriptionInput'}
            onChange={e => changeFilterValues('description', e.target.value, categoryState.codelists)}
            value={categoryState.filter.description}
          />
          <label htmlFor={'filterDescriptionInput'}>{resources.messages['codelistDescription']}</label>
        </span>
        {renderFilterOrder('description')}
      </div>
    );
  };

  const statusTemplate = option => (
    <span className={`${getStatusStyle(option.value)} ${styles.statusBox}`}>{option.statusType}</span>
  );

  // const updateEditingCodelists = isNewEditingCodelist => {
  //   if (isNewEditingCodelist) {
  //     dispatchCategory({ type: 'UPDATE_EDITING_CODELISTS', payload: 1 });
  //   } else {
  //     dispatchCategory({ type: 'UPDATE_EDITING_CODELISTS', payload: -1 });
  //   }
  // };

  return (
    <React.Fragment>
      <TreeViewExpandableItem
        buttons={[
          {
            disabled: !checkNoCodelistEditing(),
            icon: 'pencil',
            label: '',
            onClick: () => toggleDialog('TOGGLE_EDIT_DIALOG_VISIBLE', true),
            tooltip: resources.messages['editCategory'],
            visible: !isInDesign || isEditionModeOn
          },
          {
            disabled: category.codelists.length > 0,
            icon: 'trash',
            label: '',
            onClick: () => toggleDialog('TOGGLE_DELETE_DIALOG_VISIBLE', true),
            tooltip: resources.messages['deleteCategory'],
            visible: !isInDesign || isEditionModeOn
          },
          {
            disabled: !checkNoCodelistEditing(),
            icon: 'add',
            label: '',
            onClick: () => toggleDialog('TOGGLE_ADD_CODELIST_DIALOG_VISIBLE', true),
            tooltip: resources.messages['newCodelist'],
            visible: !isInDesign || isEditionModeOn
          }
        ]}
        className={styles.categoryExpandable}
        // expanded={CategoryUtils.getCategoryById(expandedStatus, categoryState.categoryId).expanded}
        expanded={collapseAll ? false : expandAll ? true : categoryState.expanded}
        items={[{ label: categoryState.categoryShortCode }, { label: categoryState.categoryDescription }]}
        onExpandTree={() => onChangeExpandedStatus(true)}
        onCollapseTree={() => onChangeExpandedStatus(false)}
        // onCalculateExpandedStatus(
        //   categoryState.categoryId,
        //   CategoryUtils.getCategoryById(expandedStatus, categoryState.categoryId).expanded
        // )
        // }
        // onExpandTree={() => onLoadCodelists()}
      >
        {
          <React.Fragment>
            {categoryState.codelists.length > 0 ? (
              renderCodelist()
            ) : (
              <div className={styles.noCodelistsMessage}>
                <span>{resources.messages['noCodelists']}</span>
              </div>
            )}
          </React.Fragment>
        }
      </TreeViewExpandableItem>
      {renderEditDialog()}
      {renderAddCodelistDialog()}
      {renderDeleteDialog()}
    </React.Fragment>
  );
};

export { Category };
