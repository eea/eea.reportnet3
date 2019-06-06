import React, {useEffect, useState} from 'react';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';


import jsonData from '../../../assets/jsons/list-of-errors.json';
import HTTPRequesterAPI from '../../../services/HTTPRequester/HTTPRequester';

const ValidationDataViewer = ()=>{
    const [fetchedData, setFetchedData] = useState(jsonData);
    const [loading, setLoading] = useState(false);
    const [cols, setCols] = useState([]);

    useEffect(() => {
        //TODO: 
        //TODO: HTTPRequesterAPI call

        setCols(Object.keys(jsonData));        

    }, []);

    // useEffect(()=>{  
    //     let columnsArr = cols.map(col => <Column sortable={true} key={col.field} field={col.field} header={col.header} />);
    //     setColumns(columnsArr); 
  
    //   }, [cols, colOptions]);


    return(
        <DataTable value={fetchedData} paginatorRight={fetchedData.totalRecords}
                    resizableColumns={true} reorderableColumns={true}
                    paginator={true} 
                    //rows={numRows} first={firstRow} onPage={onChangePageHandler} 
                    rowsPerPageOptions={[5, 10, 20, 100]} lazy={true} 
                    loading={loading} totalRecords={fetchedData.totalRecords} sortable={true}
                    //onSort={onSortHandler} header={header} sortField={sortField} sortOrder={sortOrder} 
                    autoLayout={true}>
                {/* {columns} */}
            </DataTable>
    );

}

export default ValidationDataViewer;

