import { Fragment, useEffect } from 'react';
import { withRouter } from 'react-router-dom';

const ScrollToTop = withRouter(({ history, children }) => {
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
});

export { ScrollToTop };
