import React, { useEffect } from 'react';

export const AccessPoint = ({ location, history }) => {
  useEffect(() => {
    try {
      const params = new URLSearchParams(location.search);
      const code = params.get('code');
      if (code) {
      } else {
        history.push();
      }
    } catch (error) {}
  }, []);

  return <div></div>;
};
