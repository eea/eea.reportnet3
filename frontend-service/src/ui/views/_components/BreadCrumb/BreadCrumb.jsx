import React from 'react';
import { BreadCrumb as PrimeBreadCrumb } from 'primereact/breadcrumb';

export const BreadCrumb = ({ className, home, id, model, style }) => {
  return <PrimeBreadCrumb className={className} home={home} id={id} model={model} style={style} />;
};
