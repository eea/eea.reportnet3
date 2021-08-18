import styles from './CharacterCounter.module.scss';

const CharacterCounter = ({ currentLength, maxLength }) => {
  return (
    <p className={currentLength > 245 ? styles.redCharacterCount : styles.characterCount}>
      {`${currentLength}/${maxLength}`}
    </p>
  );
};
export { CharacterCounter };
