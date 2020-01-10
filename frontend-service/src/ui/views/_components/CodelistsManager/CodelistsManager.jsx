import React, { useEffect, useContext, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { isUndefined, isNull } from 'lodash';

import styles from './CodelistsManager.module.css';

import { Button } from 'ui/views/_components/Button';
import { Category } from './_components/Category';
import { CodelistsForm } from './_components/CodelistsForm';
import { InputText } from 'ui/views/_components/InputText';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { routes } from 'ui/routes';

import { CodelistsManagerUtils } from './_functions/Utils/CodelistsManagerUtils';

const CodelistsManager = ({ isDataCustodian = true, isInDesign = false, setIsLoading }) => {
  const resources = useContext(ResourcesContext);
  const [categories, setCategories] = useState([]);
  const [filter, setFilter] = useState();
  const [filteredCategories, setFilteredCategories] = useState([]);
  const [isFiltered, setIsFiltered] = useState(false);
  const [newCategory, setNewCategory] = useState({ name: '', description: '' });
  const [newCategoryVisible, setNewCategoryVisible] = useState(false);

  useEffect(() => {
    setIsLoading(true);
    try {
      onLoadCategories();
    } catch (error) {
      console.error(error.response);
    } finally {
      setIsLoading(false);
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

  const onFilter = filter => {
    setIsFiltered(filter === '');

    const inmCategories = [...categories];
    console.log(CodelistsManagerUtils.filterByText(inmCategories, filter.toUpperCase()));
    //const filteredCategories = CodelistsManagerUtils.filterByText(inmCategories, filter);
    setFilteredCategories(CodelistsManagerUtils.filterByText(inmCategories, filter.toUpperCase()));
    setFilter(filter);
  };

  const onLoadCategories = async () => {
    try {
      const loadedCategories = [
        {
          name: 'wise',
          description: '(WISE - Water Information System of Europe)',
          codelists: [
            {
              name: 'BWDObservationStatus',
              description: '(Bathing water observation status)',
              version: '1.0',
              status: 'Ready',
              items: [
                {
                  itemId: '1',
                  code: 'confirmedValue',
                  label: 'Confirmed value',
                  definition: 'Status flag to confirm that the reported observation value is...'
                },
                {
                  itemId: '2',
                  code: 'limitOfDetectionValue',
                  label: 'Limit of detection value',
                  definition: 'Status flag to inform that a specific observed...'
                }
              ]
            },
            {
              name: 'BWDStatus',
              description: '(Bathing water quality) ',
              version: '3.0',
              status: 'Design',
              items: [
                {
                  itemId: '3',
                  code: 0,
                  label: 'Not classified',
                  definition: 'Bathing water quality cannot be assessed and classified.'
                },
                {
                  itemId: '4',
                  code: 1,
                  label: 'Excellent',
                  definition:
                    'See Annex II (4) of BWD. Bathing water quality status is Excellent if: for inland waters, ( p95(IE) <= 200 ) AND ( p95(EC) <= 500 ) ...'
                }
              ]
            },
            {
              name: 'BWDStatus',
              description: '(Bathing water quality) ',
              version: '3.1',
              status: 'Design',
              items: [
                {
                  itemId: '5',
                  code: 0,
                  label: 'Not classified',
                  definition: 'Bathing water quality cannot be assessed and classified.'
                },
                {
                  itemId: '6',
                  code: 1,
                  label: 'Excellent',
                  definition:
                    'See Annex II (4) of BWD. Bathing water quality status is Excellent if: for inland waters, ( p95(IE) <= 200 ) AND ( p95(EC) <= 500 ) ...'
                }
              ]
            }
          ]
        },
        {
          name: 'category 2',
          description: '(Category 2 - Fire Information System of Europe)',
          codelists: []
        }
      ];
      // await Categorieservice.all(`${match.params.dataflowId}`);
      // loadedCategories = sortBy(loadedCategories, ['Document', 'id']);
      setCategories(loadedCategories);
    } catch (error) {
    } finally {
      setIsLoading(false);
    }
  };

  const onSaveCategory = () => {
    //API CALL
    //Meanwhile....
    const inmCategories = [...categories];
    newCategory.codelists = [];
    inmCategories.push(newCategory);
    setCategories(inmCategories);
    setNewCategoryVisible(false);
  };

  const checkDuplicates = (codelistName, codelistVersion) => {
    if (!isUndefined(categories) && !isNull(categories)) {
      const inmCategories = [...categories];
      console.log({ inmCategories });

      const repeteadElements = inmCategories.filter(
        category =>
          category.codelists.filter(
            codelist =>
              codelistName.toLowerCase() === codelist.name.toLowerCase() &&
              codelistVersion.toLowerCase() === codelist.version.toLowerCase()
          ).length > 0
      );
      return repeteadElements.length > 0; //&& fieldId !== repeteadElements[0].fieldId;
    } else {
      return false;
    }
  };

  const renderCategories = data =>
    data.map(category => {
      return (
        <Category
          category={category}
          checkDuplicates={checkDuplicates}
          isDataCustodian={isDataCustodian}
          isInDesign={isInDesign}
        />
      );
    });

  return (
    <React.Fragment>
      <div className={styles.codelistsActions}>
        <span className={`${styles.filterSpan} p-float-label`}>
          <InputText id="filterInput" onChange={e => onFilter(e.target.value)} value={filter} />
          <label htmlFor="filterInput">{resources.messages['filterCodelists']}</label>
        </span>
        {isDataCustodian ? (
          <Button
            label={resources.messages['newCategory']}
            icon="add"
            onClick={() => setNewCategoryVisible(true)}
            style={{ marginRight: '1.5rem' }}
          />
        ) : null}
      </div>
      <hr />
      {isFiltered ? renderCategories(filteredCategories) : renderCategories(categories)}
      <CodelistsForm
        newCategory={newCategory}
        columns={['name', 'description']}
        onChangeCategoryForm={onChangeCategoryForm}
        onHideDialog={() => {
          setNewCategory({ name: '', description: '' });
          setNewCategoryVisible(false);
        }}
        onSaveCategory={onSaveCategory}
        visible={newCategoryVisible}
      />
    </React.Fragment>
  );
};

export { CodelistsManager };
