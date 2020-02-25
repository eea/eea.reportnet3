import React, { useContext, useEffect, useState } from 'react';

import styles from './UserObligations.module.scss';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { TreeViewExpandableItem } from 'ui/views/_components/TreeView/_components/TreeViewExpandableItem';

const UserObligations = () => {
  const resources = useContext(ResourcesContext);

  const [dataflowData, setDataflowData] = useState([]);

  useEffect(() => {
    onLoadDataflowData();
  }, []);

  const onLoadDataflowData = () => {
    const arrayDataflow = [
      {
        dataflow: 'dataflow1',
        dataset: ['dataset1 (Reporting)', 'dataset1 (Schema)']
      },
      {
        dataflow: 'dataflow2',
        dataset: ['dataset2 (Reporting)', 'dataset2 (Schema)']
      }
    ];
    setDataflowData(arrayDataflow);
  };

  const renderChildrenArray = () => {
    return dataflowData.map((item, index) => (
      <TreeViewExpandableItem
        className={styles.obligationsExpandable}
        expanded={false}
        items={[{ label: item.dataflow }]}
        key={index}
        children={['dataflow1  ']}>
        {item.dataset.map((dataset, i) => (
          <ul>
            <li className={styles.children} key={i}>
              {dataset}
            </li>
          </ul>
        ))}
      </TreeViewExpandableItem>
    ));
  };

  return (
    <div className={styles.userObligationsContainer}>
      <div className={styles.userObligationsTitle}>{resources.messages['obligations']}</div>
      {renderChildrenArray()}
    </div>
  );
};

export { UserObligations };
