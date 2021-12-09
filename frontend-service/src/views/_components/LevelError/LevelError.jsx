import styles from './LevelError.module.scss';

export const LevelError = ({ type, category = undefined }) => {
  const getClassName = (type, category) => {
    const className = category === 'LEVEL_ERROR' ? styles[type.toString().toLowerCase()] : '';
    return className;
  };

  return <span className={`${getClassName(type, category)} ${styles.statusBox}`}>{type.toString().toUpperCase()}</span>;
};
