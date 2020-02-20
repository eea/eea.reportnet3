import React, { useContext, useEffect, useState } from 'react';

import styles from './UserObligations.module.scss';

import { TreeViewExpandableItem } from 'ui/views/_components/TreeView/_components/TreeViewExpandableItem';

const UserObligations = () => {
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
    return dataflowData.map(item => (
      <TreeViewExpandableItem
        className={styles.obligationsExpandable}
        expanded={false}
        items={[{ label: item.dataflow }]}
        children={['dataflow1  ']}>
        {item.dataset.map(dataset => (
          <ul>
            <li className={styles.children}>{dataset}</li>
          </ul>
        ))}
      </TreeViewExpandableItem>
    ));
  };

  return (
    <div className={styles.userObligationsContainer}>
      <div className={styles.userObligationsTitle}>Obligations</div>
      {renderChildrenArray()}
    </div>
  );
};

export { UserObligations };
