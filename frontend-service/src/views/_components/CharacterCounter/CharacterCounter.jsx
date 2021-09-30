import isNil from 'lodash/isNil';

import styles from './CharacterCounter.module.scss';

const CharacterCounter = ({ currentLength, inputRef, maxLength, style }) => {
  const getCounterClassName = () => {
    if (isNil(maxLength)) {
      return '';
    }

    if (currentLength > maxLength) {
      return styles.errorCharacterCount;
    }

    if (maxLength - currentLength <= 10) {
      return styles.warningCharacterCount;
    }

    return '';
  };

  return (
    <p className={`${styles.characterCount} ${getCounterClassName()}`} ref={inputRef} style={style}>
      {isNil(maxLength) ? `${currentLength}` : `${currentLength}/${maxLength}`}
    </p>
  );
};

export { CharacterCounter };
