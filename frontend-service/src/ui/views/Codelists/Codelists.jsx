import React, { useEffect, useContext, useState } from 'react';
import { withRouter } from 'react-router-dom';

import styles from './Codelists.module.css';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { Codelist } from './_components/Codelist';
import { InputText } from 'ui/views/_components/InputText';
import { MainLayout } from 'ui/views/_components/Layout';
import { Spinner } from 'ui/views/_components/Spinner';
import { Title } from 'ui/views/_components/Title';
import { TreeViewExpandableItem } from 'ui/views/_components/TreeView/_components/TreeViewExpandableItem';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { routes } from 'ui/routes';

const Codelists = withRouter(({ match, history }) => {
  const resources = useContext(ResourcesContext);

  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [categories, setCategories] = useState([]);
  const [filteredCategories, setFilteredCategories] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

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

  const onFilter = filter => {
    // const inmCategories =
  };

  const onLoadCategories = async () => {
    try {
      const loadedCategories = {
        categories: [
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
        ]
      };
      // await Categorieservice.all(`${match.params.dataflowId}`);
      // loadedCategories = sortBy(loadedCategories, ['Document', 'id']);
      setCategories(loadedCategories);
    } catch (error) {
    } finally {
      setIsLoading(false);
    }
  };

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
      <span className="p-float-label">
        <InputText id="filterInput" onKeyDown={e => onFilter(e.target.value)} />
        <label htmlFor="filterInput">{resources.messages['filterCodelists']}</label>
      </span>
      <hr />
      {categories.categories.map(category => {
        return (
          <TreeViewExpandableItem
            className={styles.categoryExpandable}
            expanded={true}
            items={[category.name, category.description]}>
            <div className={styles.codelists}>
              {category.codelists.map(codelist => {
                return <Codelist codelist={codelist} />;
              })}
            </div>
          </TreeViewExpandableItem>
        );
      })}
    </React.Fragment>
  );
});

export { Codelists };
