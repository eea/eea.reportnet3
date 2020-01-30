import React, { useEffect, useContext, useState, useReducer } from 'react';
import { withRouter } from 'react-router-dom';

import { isUndefined, isNull, cloneDeep } from 'lodash';

import styles from './CodelistsManager.module.css';

import { Button } from 'ui/views/_components/Button';
import { Category } from './_components/Category';
import { CategoryForm } from './_components/CategoryForm';
import { Dialog } from 'ui/views/_components/Dialog';
import { InputSwitch } from 'ui/views/_components/InputSwitch';
import { InputText } from 'ui/views/_components/InputText';
import { Spinner } from 'ui/views/_components/Spinner';

import { CodelistCategoryService } from 'core/services/CodelistCategory';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { routes } from 'ui/routes';

import { codelistsManagerReducer } from './_functions/Reducers/codelistsManagerReducer';

import { CodelistsManagerUtils } from './_functions/Utils/CodelistsManagerUtils';

const CodelistsManager = ({ isDataCustodian = true, isInDesign = false, onCodelistSelected }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [codelistsInEdition, setCodelistsInEdition] = useState(0);
  const [errorMessage, setErrorMessage] = useState('');
  const [errorMessageTitle, setErrorMessageTitle] = useState('');
  const [isEditionModeOn, setIsEditionModeOn] = useState(false);
  const [isErrorDialogVisible, setIsErrorDialogVisible] = useState(false);
  const [isIncorrect, setIsIncorrect] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [newCategory, setNewCategory] = useState({ shortCode: '', description: '' });
  const [newCategoryVisible, setNewCategoryVisible] = useState(false);

  const initialCodelistsManagerState = {
    categories: [],
    categoriesExpandStatus: [],
    collapseAll: false,
    filter: undefined,
    filteredCategories: [],
    isFiltered: false,
    order: 1,
    expandAll: false,
    toggleExpandCollapseAll: 0
  };
  const [codelistsManagerState, dispatchCodelistsManager] = useReducer(
    codelistsManagerReducer,
    initialCodelistsManagerState
  );

  useEffect(() => {
    try {
      onLoadCategories();
    } catch (error) {
      console.error(error.response);
    } finally {
    }
  }, []);

  useEffect(() => {
    if (codelistsManagerState.isFiltered) {
      onFilter(codelistsManagerState.filter);
    }
    onCalculateInitialExpandedStatus();
  }, [codelistsManagerState.categories]);

  useEffect(() => {
    if (isErrorDialogVisible) {
      renderErrors(errorMessageTitle, errorMessage);
    }
  }, [isErrorDialogVisible]);

  const onCalculateInitialExpandedStatus = () => {
    dispatchCodelistsManager({ type: 'SET_INITIAL_EXPANDED_STATUS' });
  };

  const onCalculateExpandedStatus = (categoryId, expanded) => {
    dispatchCodelistsManager({ type: 'SET_EXPANDED_STATUS', payload: { categoryId, expanded } });
  };

  const onChangeCategoryForm = (property, value) => {
    const inmNewCategory = { ...newCategory };
    inmNewCategory[property] = value;
    setNewCategory(inmNewCategory);
  };

  const onCodelistError = (errorTitle, error) => {
    setErrorMessageTitle(errorTitle);
    setErrorMessage(error);
    setIsErrorDialogVisible(true);
  };

  const onFilter = filter => {
    const inmCategories = cloneDeep(codelistsManagerState.categories);
    // const filteredCategories = inmCategories.filter(category =>
    //   category.shortCode.toLowerCase().includes(filter.toLowerCase())
    // );
    const filteredCategories = CodelistsManagerUtils.filterByText(cloneDeep(inmCategories), filter.toUpperCase());
    dispatchCodelistsManager({ type: 'SET_FILTER', payload: { data: filteredCategories, filter } });
  };

  const onLoadCategories = async () => {
    try {
      setIsLoading(true);
      const loadedCategories = await CodelistCategoryService.all();
      dispatchCodelistsManager({ type: 'SET_CATEGORIES', payload: { categories: loadedCategories } });
    } catch (error) {
      console.error(error);
      notificationContext.add({
        type: 'CODELIST_CATEGORY_SERVICE_ALL_ERROR'
      });
    } finally {
      setIsLoading(false);
    }
  };

  const onOrderCategories = order => {
    dispatchCodelistsManager({ type: 'ORDER_CATEGORIES', payload: { order } });
  };

  // const onLoadCategory = async (categoryId) => {
  //   try {
  //     setIsLoading(true);
  //     const loadedCategory = await CodelistCategoryService.allInCategory(categoryId);
  //     const inmCategories = cloneDeep(categories);
  //     inmCategories[CodelistsManagerUtils.getCategoryById(inmCategories,categoryId)] = loadedCategory;
  //     setCategories(inmCategories);
  //   } catch (error) {
  //     console.error(error);
  //     notificationContext.add({
  //       type: 'CODELIST_CATEGORY_SERVICE_ALL_IN_CATEGORY_ERROR'
  //     });
  //   } finally {
  //     setIsLoading(false);
  //   }
  // };

  const onSaveCategory = async () => {
    try {
      const response = await CodelistCategoryService.addById(newCategory.shortCode, newCategory.description);
      if (response.status >= 200 && response.status <= 299) {
        onLoadCategories();
      }
    } catch (error) {
      console.error(error);
      notificationContext.add({
        type: 'CODELIST_CATEGORY_SERVICE_ADD_BY_ID_ERROR'
      });
    } finally {
      setNewCategoryVisible(false);
    }
  };

  const onToggleIncorrect = isIncorrect => {
    setIsIncorrect(isIncorrect);
  };

  const onToggleExpandCollapseAll = () => {
    console.log(codelistsManagerState.toggleExpandCollapseAll);
    dispatchCodelistsManager({
      type: codelistsManagerState.toggleExpandCollapseAll === 0 ? 'EXPAND_ALL' : 'COLLAPSE_ALL'
    });
  };

  const checkCategoryDuplicates = (categoryShortCode, categoryId) => {
    const inmCategories = [...codelistsManagerState.categories];
    const repeatedElements = inmCategories.filter(category => category.shortCode.toLowerCase() === categoryShortCode);
    return isUndefined(categoryId)
      ? repeatedElements.length > 0
      : repeatedElements.length > 0 && categoryId !== repeatedElements[0].id;
  };

  const checkDuplicates = (codelistName, codelistVersion, codelistId, isCloning) => {
    if (!isUndefined(codelistsManagerState.categories) && !isNull(codelistsManagerState.categories)) {
      const inmCategories = [...codelistsManagerState.categories];
      const codelists = inmCategories.map(category => category.codelists).flat();
      const repeatedElements = codelists.filter(
        codelist =>
          codelistName.toLowerCase() === codelist.name.toLowerCase() &&
          codelistVersion.toLowerCase() === codelist.version.toLowerCase()
      );
      return isCloning
        ? repeatedElements.length > 0 && codelistId
        : repeatedElements.length > 0 && codelistId !== repeatedElements[0].id;
    } else {
      return false;
    }
  };

  const checkNoCodelistEditing = () => codelistsInEdition === 0;

  const errorDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        label={resources.messages['ok']}
        icon="check"
        onClick={() => {
          setIsErrorDialogVisible(false);
        }}
      />
    </div>
  );

  const renderCategories = data =>
    data.map((category, i) => {
      return (
        <Category
          categoriesDropdown={codelistsManagerState.categories.map(category => {
            return { categoryType: category.shortCode, value: category.id };
          })}
          category={cloneDeep(category)}
          expandedStatus={codelistsManagerState.categoriesExpandStatus}
          checkCategoryDuplicates={checkCategoryDuplicates}
          checkDuplicates={checkDuplicates}
          checkNoCodelistEditing={checkNoCodelistEditing}
          collapseAll={codelistsManagerState.collapseAll}
          expandAll={codelistsManagerState.expandAll}
          isDataCustodian={isDataCustodian}
          isEditionModeOn={isEditionModeOn}
          isIncorrect={isIncorrect}
          isInDesign={isInDesign}
          key={i}
          onCalculateExpandedStatus={onCalculateExpandedStatus}
          onCodelistError={onCodelistError}
          onCodelistSelected={onCodelistSelected}
          onLoadCategories={onLoadCategories}
          // onLoadCategory={onLoadCategory}
          onToggleIncorrect={onToggleIncorrect}
          updateEditingCodelists={updateEditingCodelists}
        />
      );
    });

  const renderErrors = (errorTitle, error) => {
    return (
      <Dialog
        footer={errorDialogFooter}
        header={errorTitle}
        modal={true}
        onHide={() => setIsErrorDialogVisible(false)}
        visible={isErrorDialogVisible}>
        <div className="p-grid p-fluid">{error}</div>
      </Dialog>
    );
  };

  const updateEditingCodelists = isNewEditingCodelist => {
    if (isNewEditingCodelist) {
      setCodelistsInEdition(codelistsInEdition + 1);
    } else {
      setCodelistsInEdition(codelistsInEdition - 1);
    }
  };

  return (
    <React.Fragment>
      {isDataCustodian && isInDesign ? (
        <div className={styles.codelistsActions}>
          <div className={styles.switchDiv}>
            <span className={styles.switchTextInput}>{resources.messages['editCodelists']}</span>
            <InputSwitch
              checked={isEditionModeOn}
              onChange={e => {
                setIsEditionModeOn(e.value);
              }}
            />
          </div>
        </div>
      ) : null}
      <div className={styles.codelistsActions}>
        {
          <span className={`${styles.filterSpan} p-float-label`}>
            <InputText
              className={styles.inputFilter}
              id="filterInput"
              onChange={e => onFilter(e.target.value)}
              value={codelistsManagerState.filter}
            />
            <label htmlFor="filterInput">{resources.messages['filterCategories']}</label>
          </span>
        }
        {
          <div>
            <Button
              className={`p-button-secondary ${styles.orderIcon}`}
              icon={codelistsManagerState.order === 1 ? 'alphabeticOrderUp' : 'alphabeticOrderDown'}
              onClick={() => onOrderCategories(codelistsManagerState.order)}
              style={{ fontSize: '12pt' }}
              tooltip={resources.messages['orderAlphabetically']}
              tooltipOptions={{ position: 'bottom' }}
            />
            <Button
              className={`p-button-secondary ${styles.orderIcon}`}
              icon={codelistsManagerState.toggleExpandCollapseAll === 1 ? 'angleRight' : 'angleDown'}
              onClick={() => onToggleExpandCollapseAll()}
              tooltip={
                codelistsManagerState.toggleExpandCollapseAll === 1
                  ? resources.messages['collapseAll']
                  : resources.messages['expandAll']
              }
              tooltipOptions={{ position: 'bottom' }}
            />
          </div>
        }

        {
          <Button
            className={styles.newCategoryButton}
            disabled={!checkNoCodelistEditing()}
            icon="add"
            label={resources.messages['newCategory']}
            onClick={() => setNewCategoryVisible(true)}
            style={{ marginRight: '1.5rem' }}
            visible={!isInDesign || isEditionModeOn}
          />
        }
      </div>
      {isLoading ? (
        <Spinner className={styles.positioning} />
      ) : codelistsManagerState.isFiltered ? (
        renderCategories(codelistsManagerState.filteredCategories)
      ) : (
        renderCategories(codelistsManagerState.categories)
      )}
      {/* {isLoading ? <Spinner className={styles.positioning} /> : renderCategories(categories)} */}
      <CategoryForm
        checkCategoryDuplicates={checkCategoryDuplicates}
        columns={['shortCode', 'description']}
        isIncorrect={isIncorrect}
        newCategory={newCategory}
        onChangeCategoryForm={onChangeCategoryForm}
        onHideDialog={() => {
          setNewCategory({ shortCode: '', description: '' });
          setNewCategoryVisible(false);
        }}
        onSaveCategory={onSaveCategory}
        onToggleIncorrect={onToggleIncorrect}
        visible={newCategoryVisible}
      />
      {renderErrors(errorMessageTitle, errorMessage)}
    </React.Fragment>
  );
};

export { CodelistsManager };
