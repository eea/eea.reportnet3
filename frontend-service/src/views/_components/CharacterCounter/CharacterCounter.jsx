import styles from './CharacterCounter.module.scss';

const CharacterCounter = ({ currentLength, maxLength }) => {
  return (
    <Fragment>
      {currentLength > 245 ? (
        <p className={styles.redCharacterCount}>{`${currentLength}/${maxLength}`}</p>
      ) : (
        <p className={styles.characterCount}>{`${currentLength}/${maxLength}`}</p>
      )}
    </Fragment>
  );
};
export { CharacterCounter };
