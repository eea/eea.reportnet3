import React, { useEffect, useContext, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { isUndefined, isNull } from 'lodash';

import styles from './CodelistsManager.module.css';

import { Button } from 'ui/views/_components/Button';
import { Category } from './_components/Category';
import { CodelistsForm } from './_components/CodelistsForm';
// import { Checkbox } from 'ui/views/_components/Checkbox';
// import { InputText } from 'ui/views/_components/InputText';
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
  const [filter, setFilter] = useState();
  // const [filteredCategories, setFilteredCategories] = useState([]);
  const [isChecked, setIsChecked] = useState(false);
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

  // useEffect(() => {
  //   console.log('LOADED');
  //   setIsLoading();
  // }, [categories]);

  const onChangeCategoryForm = (property, value) => {
    const inmNewCategory = { ...newCategory };
    inmNewCategory[property] = value;
    setNewCategory(inmNewCategory);
  };

  // const onFilter = filter => {
  //   setIsFiltered(filter === '');

  //   const inmCategories = [...categories];
  //   console.log(CodelistsManagerUtils.filterByText(inmCategories, filter.toUpperCase()));
  //   //const filteredCategories = CodelistsManagerUtils.filterByText(inmCategories, filter);
  //   // setFilteredCategories(CodelistsManagerUtils.filterByText(inmCategories, filter.toUpperCase()));
  //   setFilter(filter);
  // };

  // const onFilterDeprecated = () => {
  //   const inmCategories = [...categories];
  // };

  const onLoadCategories = async () => {
    try {
      setIsLoading(true);
      const loadedCategories = await CodelistCategoryService.all();
      setCategories(loadedCategories);
    } catch (error) {
      console.log(error);
    } finally {
      setIsLoading(false);
    }
  };

  const onSaveCategory = async () => {
    //API CALL
    //Meanwhile....
    // const inmCategories = [...categories];
    // newCategory.codelists = [];
    // inmCategories.push(newCategory);
    // setCategories(inmCategories);
    // setNewCategoryVisible(false);
    try {
      const response = await CodelistCategoryService.addById(newCategory.shortCode, newCategory.description);
      if (response.status >= 200 && response.status <= 299) {
        onLoadCategories();
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
      setNewCategoryVisible(false);
    }
  };

  const checkDuplicates = (codelistName, codelistVersion) => {
    if (!isUndefined(categories) && !isNull(categories)) {
      const inmCategories = [...categories];
      console.log({ inmCategories });

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
          onCodelistSelected={onCodelistSelected}
          onLoadCategories={onLoadCategories}
        />
      );
    });

  return (
    <React.Fragment>
      <div className={styles.codelistsActions}>
        {/* <span className={`${styles.filterSpan} p-float-label`}>
          <InputText id="filterInput" onChange={e => onFilter(e.target.value)} value={filter} />
          <label htmlFor="filterInput">{resources.messages['filterCodelists']}</label>
        </span>
        <Checkbox
          className={styles.filterDeprecatedCheckbox}
          defaultChecked={false}
          id="filterDeprecated"
          isChecked={isChecked}
          onChange={() => {
            onFilterDeprecated();
            setIsChecked(!isChecked);
          }}
          htmlFor="filterDeprecated"
          labelClassName={styles.filterDeprecatedLabel}
          labelMessage={resources.messages['showDeprecatedCodelists']}
        /> */}
        {isDataCustodian ? (
          <Button
            label={resources.messages['newCategory']}
            icon="add"
            onClick={() => setNewCategoryVisible(true)}
            style={{ marginRight: '1.5rem' }}
          />
        ) : null}
      </div>
      {/* {isFiltered ? renderCategories(filteredCategories) : renderCategories(categories)} */}
      {console.log({ categories })}
      {isLoading ? <Spinner className={styles.positioning} /> : renderCategories(categories)}
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
    </React.Fragment>
  );
};

export { CodelistsManager };
