import styles from './LevelError.module.scss';

export const LevelError = ({ type, value = '' }) => {
  const getOption = (type, value) => {
    return value.length > 0 ? value.toUpperCase() : type.toString().toUpperCase();
  };

  return (
    <span className={`${styles[type.toString().toLowerCase()]} ${styles.statusBox}`}>{getOption(type, value)}</span>
  );
};
