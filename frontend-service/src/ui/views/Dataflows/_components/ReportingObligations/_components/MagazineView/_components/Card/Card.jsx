import React from 'react';

export const Card = ({ ids, headers, footers, titles, subTitles, style, className, childrens }) => {
  let header, body;

  const renderHeader = () => <div className="p-card-header">{headers}</div>;

  const renderBody = () => {
    let title, subTitle, footer, children;

    if (titles) title = <div className="p-card-title">{titles}</div>;

    if (subTitles) subTitle = <div className="p-card-subtitle">{subTitles}</div>;

    if (footers) footer = <div className="p-card-footer"> {footers}</div>;

    if (childrens) children = <div className="p-card-content"> {childrens} </div>;

    return (
      <div className="p-card-body">
        {title}
        {subTitle}
        {children}
        {footer}
      </div>
    );
  };

  if (header) header = renderHeader();

  body = renderBody();

  return (
    <div className={`p-card p-component ${className}`} style={style}>
      {header}
      {body}
    </div>
  );
};
