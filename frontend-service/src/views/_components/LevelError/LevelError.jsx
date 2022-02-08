import styles from './LevelError.module.scss';

export const LevelError = ({ type }) => (
  <span className={`${styles[type.toLowerCase()]} ${styles.statusBox}`}>{type.toUpperCase()}</span>
);
