import styles from './CharacterCounter.module.scss';

const CharacterCounter = ({ currentLength, inputRef, maxLength, style }) => {
  return (
    <p
      className={`${styles.characterCount} ${
        currentLength > maxLength
          ? styles.errorCharacterCount
          : maxLength - currentLength <= 10
          ? styles.warningCharacterCount
          : ''
      }`}
      ref={inputRef}
      style={style}>
      {`${currentLength}/${maxLength}`}
    </p>
  );
};
export { CharacterCounter };
