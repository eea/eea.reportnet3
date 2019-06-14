import React from 'react';

const ReporterDataSetContext = React.createContext({
        validationsVisibleHandler:null,
        setTabHandler: null,
        dataShowValidations: [] 
    });

export default ReporterDataSetContext;