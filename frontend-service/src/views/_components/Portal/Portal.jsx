import { useEffect, useState } from 'react';
import ReactDOM from 'react-dom';

export const Portal = ({ children, className = 'root-portal', element = 'div' }) => {
  const [container] = useState(() => {
    const el = document.createElement(element);
    el.classList.add(className);
    return el;
  });

  useEffect(() => {
    document.body.appendChild(container);
    return () => {
      document.body.removeChild(container);
    };
  }, []);

  return ReactDOM.createPortal(children, container);
};
