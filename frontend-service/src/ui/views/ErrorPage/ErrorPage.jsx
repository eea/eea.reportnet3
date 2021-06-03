import React from 'react';

import { MainLayout } from 'ui/views/_components/Layout/MainLayout';
import { ErrorBoundaryFallback } from 'ui/views/_components/ErrorBoundaryFallback';

export const ErrorPage = () => {
  return <ErrorBoundaryFallback error={`{message:"error", stack:"stack"}`} resetErrorBoundaty={() => {}} />;
};
