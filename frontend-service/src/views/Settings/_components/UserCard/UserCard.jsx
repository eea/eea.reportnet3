import styles from './UserCard.module.scss';

import { UserData } from './_components/UserData';

export const UserCard = () => (
  <div className={`${styles.userBoxContainer}`}>
    <UserData />
  </div>
);
