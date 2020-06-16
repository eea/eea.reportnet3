import { useEffect } from 'react';

export const useLockBodyScroll = visible => {
  useEffect(() => {
    const originalStyle = window.getComputedStyle(document.body).overflow;
    document.body.style.overflow = 'hidden';
    return () => (document.body.style.overflow = originalStyle);
  }, [visible]);
};
