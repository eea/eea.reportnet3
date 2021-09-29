import isNil from 'lodash/isNil';

import styles from './CharacterCounter.module.scss';

const CharacterCounter = ({ currentLength, inputRef, maxLength, style }) => {
  const getCounterClassName = () => {
    if (isNil(maxLength)) return '';

    return currentLength > maxLength
      ? styles.errorCharacterCount
      : maxLength - currentLength <= 10
      ? styles.warningCharacterCount
      : '';
  };

  return (
    <p className={`${styles.characterCount} ${getCounterClassName()}`} ref={inputRef} style={style}>
      {isNil(maxLength) ? `${currentLength}` : `${currentLength}/${maxLength}`}
    </p>
  );
};

export { CharacterCounter };
