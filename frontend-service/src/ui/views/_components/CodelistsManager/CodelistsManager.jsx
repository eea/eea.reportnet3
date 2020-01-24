import React, { useEffect, useContext, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { isUndefined, isNull } from 'lodash';

import styles from './CodelistsManager.module.css';

import { Button } from 'ui/views/_components/Button';
import { Category } from './_components/Category';
import { CodelistsForm } from './_components/CodelistsForm';
import { Dialog } from 'ui/views/_components/Dialog';
import { InputText } from 'ui/views/_components/InputText';
import { Spinner } from 'ui/views/_components/Spinner';

import { CodelistCategoryService } from 'core/services/CodelistCategory';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { routes } from 'ui/routes';

import { CodelistsManagerUtils } from './_functions/Utils/CodelistsManagerUtils';

const CodelistsManager = ({ isDataCustodian = true, isInDesign = false, onCodelistSelected }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [categories, setCategories] = useState([]);
  const [errorMessage, setErrorMessage] = useState('');
  const [errorMessageTitle, setErrorMessageTitle] = useState('');
  const [filter, setFilter] = useState();
  const [filteredCategories, setFilteredCategories] = useState([]);
  const [isErrorDialogVisible, setIsErrorDialogVisible] = useState(false);
  const [isFiltered, setIsFiltered] = useState(false);
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
      console.log(error);
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
      console.log(error);
      notificationContext.add({
        type: 'CODELIST_CATEGORY_SERVICE_ADD_BY_ID_ERROR'
      });
    } finally {
      setNewCategoryVisible(false);
    }
  };

  const checkDuplicates = (codelistName, codelistVersion) => {
    if (!isUndefined(categories) && !isNull(categories)) {
      const inmCategories = [...categories];

      const repeteadElements = inmCategories.filter(
        category =>
          category.codelists.filter(
            codelist =>
              codelistName.toLowerCase() === codelist.shortCode.toLowerCase() &&
              codelistVersion.toLowerCase() === codelist.version.toLowerCase()
          ).length > 0
      );
      return repeteadElements.length > 0; //&& fieldId !== repeteadElements[0].fieldId;
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
          checkDuplicates={checkDuplicates}
          isDataCustodian={isDataCustodian}
          isInDesign={isInDesign}
          key={i}
          onCodelistError={onCodelistError}
          onCodelistSelected={onCodelistSelected}
          onLoadCategories={onLoadCategories}
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
          <Button
            className={styles.newCategoryButton}
            icon="add"
            label={resources.messages['newCategory']}
            onClick={() => setNewCategoryVisible(true)}
            style={{ marginRight: '1.5rem' }}
          />
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
      <CodelistsForm
        newCategory={newCategory}
        columns={['shortCode', 'description']}
        onChangeCategoryForm={onChangeCategoryForm}
        onHideDialog={() => {
          setNewCategory({ shortCode: '', description: '' });
          setNewCategoryVisible(false);
        }}
        onSaveCategory={onSaveCategory}
        visible={newCategoryVisible}
      />
      {renderErrors(errorMessageTitle, errorMessage)}
    </React.Fragment>
  );
};

export { CodelistsManager };
