import { Fragment, useEffect } from 'react';
import { useHistory } from 'react-router-dom';

const ScrollToTop = ({ children }) => {
  const history = useHistory();

  useEffect(() => {
    const unlisten = history.listen(() => {
      window.scrollTo(0, 0);
    });
    return () => {
      unlisten();
    };
  }, []);

  // eslint-disable-next-line react/jsx-no-useless-fragment
  return <Fragment>{children}</Fragment>;
};

export { ScrollToTop };
