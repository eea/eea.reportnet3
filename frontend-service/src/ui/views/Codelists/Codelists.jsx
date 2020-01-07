import React, { useEffect, useContext, useState } from 'react';
import { withRouter } from 'react-router-dom';

import styles from './Codelists.module.css';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { Button } from 'ui/views/_components/Button';
import { Codelist } from './_components/Codelist';
import { CodelistsForm } from './_components/CodelistsForm';
import { InputText } from 'ui/views/_components/InputText';
import { MainLayout } from 'ui/views/_components/Layout';
import { Spinner } from 'ui/views/_components/Spinner';
import { Title } from 'ui/views/_components/Title';
import { TreeViewExpandableItem } from 'ui/views/_components/TreeView/_components/TreeViewExpandableItem';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { routes } from 'ui/routes';

import { CodelistsUtils } from './_functions/Utils/CodelistsUtils';

const Codelists = withRouter(({ match, history, isDataCustodian = true }) => {
  const resources = useContext(ResourcesContext);
  const [newCategory, setNewCategory] = useState({ name: '', description: '' });
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [categories, setCategories] = useState([]);
  const [filteredCategories, setFilteredCategories] = useState([]);
  const [isFiltered, setIsFiltered] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
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

  useEffect(() => {
    setBreadCrumbItems([{ label: resources.messages['codelists'], icon: 'home' }]);
  }, [history, resources.messages]);

  const onChangeCategoryForm = (property, value) => {
    const inmNewCategory = { ...newCategory };
    inmNewCategory[property] = value;
    setNewCategory(inmNewCategory);
  };

  const onFilter = filter => {
    if (filter === '') {
      setIsFiltered(false);
    } else {
      setIsFiltered(true);
    }

    const inmCategories = [...categories];
    //const filteredCategories = CodelistsUtils.filterByText(inmCategories, filter);
    setFilteredCategories(CodelistsUtils.filterByText(inmCategories, filter));
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

  const renderCategories = data =>
    data.map(category => {
      return (
        <TreeViewExpandableItem
          className={styles.categoryExpandable}
          expanded={true}
          items={[category.name, category.description]}>
          {isDataCustodian ? (
            <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
              <Button
                label={resources.messages['editCategory']}
                icon="pencil"
                onClick={() => setNewCategoryVisible(true)}
                style={{ float: 'right' }}
              />
            </div>
          ) : null}
          <div className={styles.codelists}>
            {category.codelists.map(codelist => {
              return <Codelist codelist={codelist} />;
            })}
          </div>
        </TreeViewExpandableItem>
      );
    });

  const layout = children => {
    return (
      <MainLayout>
        <BreadCrumb model={breadCrumbItems} />
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  if (isLoading) {
    return layout(<Spinner />);
  }

  return layout(
    <React.Fragment>
      <Title title={`${resources.messages['codelists']} `} icon="list" iconSize="3.5rem" />
      <div className={styles.codelistsActions}>
        <span className={`${styles.filterSpan} p-float-label`}>
          <InputText id="filterInput" onKeyDown={e => onFilter(e.target.value.toUpperCase)} />
          <label htmlFor="filterInput">{resources.messages['filterCodelists']}</label>
        </span>
        {isDataCustodian ? (
          <Button label={resources.messages['newCategory']} icon="add" onClick={() => setNewCategoryVisible(true)} />
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
});

export { Codelists };
