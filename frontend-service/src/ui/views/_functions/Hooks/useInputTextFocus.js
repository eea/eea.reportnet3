import { useEffect } from 'react';

export const useInputTextFocus = (visibility, ref) => {
  useEffect(() => {
    if (ref.current && visibility) {
      ref.current.element.focus();
    }
  }, [ref.current, visibility]);
};
