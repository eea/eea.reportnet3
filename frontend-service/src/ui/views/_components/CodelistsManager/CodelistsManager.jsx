import React, { useEffect, useContext, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { isUndefined, isNull } from 'lodash';

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

const CodelistsManager = ({ isDataCustodian = true, isInDesign = false, onCodelistSelected }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [categories, setCategories] = useState([]);
  const [errorMessage, setErrorMessage] = useState('');
  const [errorMessageTitle, setErrorMessageTitle] = useState('');
  const [filter, setFilter] = useState();
  const [filteredCategories, setFilteredCategories] = useState([]);
  const [isEditionModeOn, setIsEditionModeOn] = useState(false);
  const [isErrorDialogVisible, setIsErrorDialogVisible] = useState(false);
  const [isFiltered, setIsFiltered] = useState(false);
  const [isIncorrect, setIsIncorrect] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [newCategory, setNewCategory] = useState({ shortCode: '', description: '' });
  const [newCategoryVisible, setNewCategoryVisible] = useState(false);

  useEffect(() => {
    try {
      onLoadCategories();
    } catch (error) {
      console.error(error.response);
    } finally {
    }
  }, []);

  useEffect(() => {
    if (isErrorDialogVisible) {
      renderErrors(errorMessageTitle, errorMessage);
    }
  }, [isErrorDialogVisible]);

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
    const inmCategories = [...categories];
    const filteredCategories = inmCategories.filter(category =>
      category.shortCode.toLowerCase().includes(filter.toLowerCase())
    );
    setFilteredCategories(filteredCategories);
    setFilter(filter);
    setIsFiltered(filter !== '');
  };

  const onLoadCategories = async () => {
    try {
      setIsLoading(true);
      const loadedCategories = await CodelistCategoryService.all();
      setCategories(loadedCategories);
    } catch (error) {
      console.error(error);
      notificationContext.add({
        type: 'CODELIST_CATEGORY_SERVICE_ALL_ERROR'
      });
    } finally {
      setIsLoading(false);
    }
  };

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

  const checkCategoryDuplicates = (categoryShortCode, categoryId) => {
    const inmCategories = [...categories];
    const repeatedElements = inmCategories.filter(category => category.shortCode.toLowerCase() === categoryShortCode);
    return isUndefined(categoryId)
      ? repeatedElements.length > 0
      : repeatedElements.length > 0 && categoryId !== repeatedElements[0].id;
  };

  const checkDuplicates = (codelistName, codelistVersion, codelistId, isCloning) => {
    if (!isUndefined(categories) && !isNull(categories)) {
      const inmCategories = [...categories];
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
          categoriesDropdown={categories.map(category => {
            return { categoryType: category.shortCode, value: category.id };
          })}
          category={category}
          checkCategoryDuplicates={checkCategoryDuplicates}
          checkDuplicates={checkDuplicates}
          isDataCustodian={isDataCustodian}
          isEditionModeOn={isEditionModeOn}
          isIncorrect={isIncorrect}
          isInDesign={isInDesign}
          key={i}
          onCodelistError={onCodelistError}
          onCodelistSelected={onCodelistSelected}
          onLoadCategories={onLoadCategories}
          onToggleIncorrect={onToggleIncorrect}
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

  return (
    <React.Fragment>
      <div className={styles.codelistsActions}>
        {
          <span className={`${styles.filterSpan} p-float-label`}>
            <InputText
              className={styles.inputFilter}
              id="filterInput"
              onChange={e => onFilter(e.target.value)}
              value={filter}
            />
            <label htmlFor="filterInput">{resources.messages['filterCategories']}</label>
          </span>
        }
        {isDataCustodian ? (
          !isInDesign ? (
            <Button
              className={styles.newCategoryButton}
              icon="add"
              label={resources.messages['newCategory']}
              onClick={() => setNewCategoryVisible(true)}
              style={{ marginRight: '1.5rem' }}
            />
          ) : (
            <div className={styles.switchDiv}>
              <span className={styles.switchTextInput}>{resources.messages['editCodelists']}</span>
              <InputSwitch
                checked={isEditionModeOn}
                onChange={e => {
                  setIsEditionModeOn(e.value);
                }}
              />
            </div>
          )
        ) : null}
      </div>
      {isLoading ? (
        <Spinner className={styles.positioning} />
      ) : isFiltered ? (
        renderCategories(filteredCategories)
      ) : (
        renderCategories(categories)
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
