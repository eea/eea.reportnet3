// dataPromise
//             .then(res =>{
//                 if(res.data.tables !== null){
//                     const tabStatisticNames = [];
//                     const tabStatisticValues = [];
//                     setDashBoardTitle(res.data.nameDataSetSchema);
//                     res.data.tables.forEach(t => {
//                         tabStatisticNames.push(t.nameTableSchema);
//                         tabStatisticValues.push([t.totalRecords-(t.totalRecordsWithErrors+t.totalRecordsWithWarnings),t.totalRecordsWithWarnings,t.totalRecordsWithErrors]);
//                     });
//                     //Transpose value matrix and delete undefined elements to fit Chart data structure
//                     const transposedValues = Object.keys(tabStatisticValues).map(c =>
//                     tabStatisticValues.map(r => r[c])
//                 ).filter(t=>t[0]!==undefined);