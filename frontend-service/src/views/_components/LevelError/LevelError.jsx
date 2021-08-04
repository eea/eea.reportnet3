import styles from './LevelError.module.scss';

export const LevelError = ({ type }) => {
  return (
    <span className={`${styles[type.toString().toLowerCase()]} ${styles.statusBox}`}>
      {type.toString().toUpperCase()}
    </span>
  );
};
