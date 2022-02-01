import styles from './TabularSwitch.module.scss';

export const TabularSwitch = ({ elements = [], onChange, value = '' }) => (
  <div className={styles.tabBar}>
    <div
      className={styles.indicator}
      style={{ left: `calc(${elements.map(element => element.key).indexOf(value) * 150}px + 1.5rem)` }}
    />
    {elements.map(element => (
      <div
        className={`${styles.tabItem} ${element.key === value ? styles.selected : null}`}
        key={element.key}
        onClick={() => onChange(element.key)}>
        <p className={styles.tabLabel}>{element.label}</p>
      </div>
    ))}
  </div>
);
