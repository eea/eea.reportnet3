import styles from './UserCard.module.scss';

import { UserData } from './_components/UserData';

const UserCard = () => {
  return (
    <div className={`${styles.userBoxContainer}`}>
      <UserData />
    </div>
  );
};

export { UserCard };
