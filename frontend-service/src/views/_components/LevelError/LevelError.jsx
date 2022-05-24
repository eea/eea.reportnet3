import styles from './LevelError.module.scss';

export const LevelError = ({ className = '', type }) => (
  <span className={`${className === '' ? styles[type.toLowerCase()] : styles[className]} ${styles.statusBox}`}>
    {type.toUpperCase()}
  </span>
);
