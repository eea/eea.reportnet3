import styles from './SnapshotsList.module.scss';

import { SnapshotItem } from './_components/SnapshotItem';

const SnapshotsList = ({ snapshotListData }) => {
  return (
    <div className={`${styles.listContainer}  ${styles.section}`}>
      <ul>
        {snapshotListData.map(item => (
          <SnapshotItem itemData={item} key={item.id} />
        ))}
      </ul>
    </div>
  );
};
export { SnapshotsList };
