/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext, useRef } from "react";
import {withRouter} from 'react-router-dom';
import styles from "./DataViewer.module.css";
import ButtonsBar from "../../../components/Layout/UI/ButtonsBar/ButtonsBar";
// import { MultiSelect } from 'primereact/multiselect';
import { DataTable } from "primereact/datatable";
import { Column } from "primereact/column";
import { Dialog } from "primereact/dialog";
import { CustomFileUpload } from "../../../components/Layout/UI/CustomFileUpload/CustomFileUpload";
import { Growl } from "primereact/growl";
import CustomIconToolTip from "../../../components/Layout/UI/CustomIconToolTip/CustomIconToolTip";
import ReporterDataSetContext from "../../../components/Context/ReporterDataSetContext";
import ResourcesContext from "../../../components/Context/ResourcesContext";
import HTTPRequester from "../../../services/HTTPRequester/HTTPRequester";
import config from "../../../conf/web.config.json";
import ConfirmDialog from "../../../components/Layout/UI/ConfirmDialog/ConfirmDialog";
import SnapshotSlideBar from "../SnapshotSlideBar/SnapshotSlideBar";

const DataViewer = props => {
	const { match:{params: { dataSetId }} } = props;
	const contextReporterDataSet = useContext(ReporterDataSetContext);
	const [importDialogVisible, setImportDialogVisible] = useState(false);
	const [totalRecords, setTotalRecords] = useState(0);
	const [fetchedData, setFetchedData] = useState([]);
	const [loading, setLoading] = useState(false);
	const [numRows, setNumRows] = useState(10);
	const [firstRow, setFirstRow] = useState(
		props.positionIdRecord && props.positionIdRecord !== null
			? Math.floor(props.positionIdRecord / numRows) * numRows
			: 0
	);
	const [sortOrder, setSortOrder] = useState();
	const [sortField, setSortField] = useState();
	const [columns, setColumns] = useState([]);
	const [cols, setCols] = useState(props.tableSchemaColumns);
	const [header] = useState();
	const [colOptions, setColOptions] = useState([{}]);
	const resources = useContext(ResourcesContext);
	const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
	const [isDataDeleted, setIsDataDeleted] = useState(false);
	const [snapshotIsVisible, setSnapshotIsVisible] = useState(false);

	let growlRef = useRef();

	useEffect(() => {
		console.log("deleted", isDataDeleted);
		setFetchedData([]);
	}, [isDataDeleted]);

	//TODO: Render se está ejecutando dos veces. Mirar por qué.
	useEffect(() => {
		console.log("Setting column options...");

		if (firstRow !== props.positionIdRecord) {
			setFirstRow(Math.floor(props.positionIdRecord / numRows) * numRows);
		}

		let colOpt = [];
		for (let col of cols) {
			colOpt.push({ label: col.header, value: col });
		}
		setColOptions(colOpt);

		console.log("Fetching data...");
		fetchDataHandler(
			null,
			sortOrder,
			Math.floor(props.positionIdRecord / numRows) * numRows,
			numRows
		);

		console.log("Filtering data...");
    const inmTableSchemaColumns = [...props.tableSchemaColumns];
    inmTableSchemaColumns.push({table: inmTableSchemaColumns[0].table, field: "id", header: ""})
		setCols(inmTableSchemaColumns);
	}, [props.positionIdRecord]);

	useEffect(() => {
		// let visibilityIcon = (<div className="TableDiv">
		//     <span className="pi pi-eye" style={{zoom:2}}></span>
		//     <span className="my-multiselected-empty-token">Visibility</span>
		//   </div>
		// );
		// let headerArr = <div className="TableDiv">
		//     <i className="pi pi-eye"></i>
		//     <MultiSelect value={cols} options={colOptions} tooltip="Filter columns" onChange={onColumnToggleHandler} style={{width:'10%'}} placeholder={visibilityIcon} filter={true} fixedPlaceholder={true}/>
		// </div>;
		// setHeader(headerArr);

    let columnsArr = cols.map(col => {
      let sort = (col.field==="id")? false : true;
      let visibleColumn = (col.field==="id")? styles.VisibleHeader : "";
      return (
			<Column
        sortable = {sort}
				key={col.field}
				field={col.field}
				header={col.header}
        body={dataTemplate}
        className={visibleColumn}
			/>
		)});
		let validationCol = (
			<Column
				key="recordValidation"
				field="validations"
				header=""
				body={validationsTemplate}
				style={{ width: "15px" }}
			/>
		);
		let newColumnsArr = [validationCol].concat(columnsArr);
		setColumns(newColumnsArr);
	}, [cols, colOptions]);

	const onChangePageHandler = event => {
		console.log("Refetching data...");
		setNumRows(event.rows);
		setFirstRow(event.first);
		contextReporterDataSet.setPageHandler(event.first);
		contextReporterDataSet.setIdSelectedRowHandler(-1);
		//fetchDataHandler(sortField, sortOrder, event.first, event.rows);
	};

	const onConfirmDeleteHandler = () => {	
		setDeleteDialogVisible(false);
		HTTPRequester.delete({
			url: `/dataset/${dataSetId}/deleteImportTable/${props.id}`,
			queryString: {}
		}).then(res => {
			setIsDataDeleted(true);
		});
	};

	const setVisibleHandler = (fnUseState, visible) => {
		fnUseState(visible);
	};

	const onSortHandler = event => {
		console.log("Sorting...");
		setSortOrder(event.sortOrder);
		setSortField(event.sortField);
		contextReporterDataSet.setPageHandler(-1);
		contextReporterDataSet.setIdSelectedRowHandler(-1);
		fetchDataHandler(event.sortField, event.sortOrder, firstRow, numRows);
	};

	const onRefreshClickHandler = () => {
		contextReporterDataSet.setPageHandler(-1);
    	contextReporterDataSet.setIdSelectedRowHandler(-1);
		fetchDataHandler(null, sortOrder, firstRow, numRows);
	};

	// const onColumnToggleHandler = (event) =>{
	//   console.log("OnColumnToggle...");
	//   setCols(event.value);
	//   setColOptions(colOptions);
	// }

	// useEffect(()=>{
	//   console.log("Fetching new data...");
	// console.log(fetchedData);
	// },[fetchedData]);

	const fetchDataHandler = (sField, sOrder, fRow, nRows) => {
		setLoading(true);

		let queryString = {
			idTableSchema: props.id,
			pageNum: Math.floor(fRow / nRows),
			pageSize: nRows
		};

		if (sField !== undefined && sField !== null) {
			queryString.fields = sField;
			queryString.asc = sOrder === -1 ? 0 : 1;
		}

		// props.urlViewer
		const dataPromise = HTTPRequester.get({
			url: props.urlViewer,
			/* url: "/jsons/response_dataset_values2.json", */
			queryString: queryString
		});
		dataPromise
			.then(response => {
				filterDataResponse(response.data);
				if (response.data.totalRecords !== totalRecords) {
					setTotalRecords(response.data.totalRecords);
				}

				setLoading(false);
			})
			.catch(error => {
				console.log(error);
				return error;
			});
	};

	const filterDataResponse = data => {
		//TODO: Refactorizar
		const dataFiltered = data.records.map(record => {
			const recordValidations = record.recordValidations;
			const arrayDataFields = record.fields.map(field => {
				return {
					fieldData: { [field.idFieldSchema]: field.value },
					fieldValidations: field.fieldValidations
				};
      });
      arrayDataFields.push({fieldData: {id: record.id}, fieldValidations:null});
			const arrayDataAndValidations = {
				dataRow: arrayDataFields,
				recordValidations
			};

			return arrayDataAndValidations;
		});

		setFetchedData(dataFiltered);
	};

	//Template for Record validation
	const validationsTemplate = (fetchedData, column) => {
		if (fetchedData.recordValidations) {
			const validations = fetchedData.recordValidations.map(
				val => val.validation
			);

			let message = "";
			validations.forEach(validation =>
				validation.message ? (message += "- " + validation.message + "\n") : ""
			);

			let levelError = "";
			let lvlFlag = 0;

			validations.forEach(validation => {
				if (validation.levelError === "WARNING") {
					const wNum = 1;
					if (wNum > lvlFlag) {
						lvlFlag = wNum;
						levelError = "WARNING";
					}
				} else if (validation.levelError === "ERROR") {
					const eNum = 2;
					if (eNum > lvlFlag) {
						lvlFlag = eNum;
						levelError = "ERROR";
					}
				} else if (validation.levelError === "BLOCKER") {
					const bNum = 2;
					if (bNum > lvlFlag) {
						lvlFlag = bNum;
						levelError = "BLOCKER";
					}
				}
			});

			return <CustomIconToolTip levelError={levelError} message={message} />;
		} else {
			return;
		}
	};

	//Template for Field validation
	const dataTemplate = (rowData, column) => {
		let row = rowData.dataRow.filter(
			r => Object.keys(r.fieldData)[0] === column.field
		)[0];
		if (row !== null && row && row.fieldValidations !== null) {
			const validations = row.fieldValidations.map(val => val.validation);
			let message = [];
			validations.forEach(validation =>
				validation.message ? (message += "- " + validation.message + "\n") : ""
			);
			let levelError = "";
			let lvlFlag = 0;
			validations.forEach(validation => {
				if (validation.levelError === "WARNING") {
					const wNum = 1;
					if (wNum > lvlFlag) {
						lvlFlag = wNum;
						levelError = "WARNING";
					}
				} else if (validation.levelError === "ERROR") {
					const eNum = 2;
					if (eNum > lvlFlag) {
						lvlFlag = eNum;
						levelError = "ERROR";
					}
				} else if (validation.levelError === "BLOCKER") {
					const bNum = 2;
					if (bNum > lvlFlag) {
						lvlFlag = bNum;
						levelError = "BLOCKER";
					}
				}
			});      

			return (
				<div
					style={{
						display: "flex",
						alignItems: "center",
						justifyContent: "space-between"
					}}
				>
					{" "}
					{row ? row.fieldData[column.field] : null}{" "}
					<CustomIconToolTip levelError={levelError} message={message} />
				</div>
			);
		} else {
			return (
				<div style={{ display: "flex", alignItems: "center" }}>
					{row ? row.fieldData[column.field] : null}
				</div>
			);
		}
	};

	//TODO: Textos + iconos + ver si deben estar aquí.
	const customButtons = [
		{
			label: resources.messages["import"],
			icon: "0",
			group: "left",
			disabled: false,
			clickHandler: () => setImportDialogVisible(true)
		},
		{
			label: resources.messages["deleteTable"],
			icon: "2",
			group: "left",
			disabled: false,
			clickHandler: () => setVisibleHandler(setDeleteDialogVisible, true)
		},
		{
			label: resources.messages["visibility"],
			icon: "6",
			group: "left",
			disabled: true,
			clickHandler: null
		},
		{
			label: resources.messages["filter"],
			icon: "7",
			group: "left",
			disabled: true,
			clickHandler: null
		},
		{
			label: resources.messages["group-by"],
			icon: "8",
			group: "left",
			disabled: true,
			clickHandler: null
		},
		{
			label: resources.messages["sort"],
			icon: "9",
			group: "left",
			disabled: true,
			clickHandler: null
		},
		{
			label: resources.messages["refresh"],
			icon: "11",
			group: "right",
			disabled: true,
			clickHandler: onRefreshClickHandler
		},
		{
			label: resources.messages["snapshots"],
			icon: "12",
			group: "right",
			disabled: false,
			clickHandler: () => setSnapshotIsVisible(true)
		}
	];

	const onHideHandler = () => {
		setImportDialogVisible(false);
	};

	const editLargeStringWithDots = (string, length) => {
		if (string.length > length) {
			return string.substring(0, length).concat("...");
		} else {
			return string;
		}
	};

	const onUploadHandler = () => {
		setImportDialogVisible(false);

		const detailContent = (
			<span>
				{resources.messages["datasetLoadingMessage"]}
				<strong>{editLargeStringWithDots(props.name, 22)}</strong>
				{resources.messages["datasetLoading"]}
			</span>
		);
		// const detailContent = <span>{resources.messages["datasetLoadingMessage"]}<strong>{props.name}</strong>{resources.messages["datasetLoading"]}</span>

		growlRef.current.show({
			severity: "info",
			summary: resources.messages["datasetLoadingTitle"],
			detail: detailContent,
			life: "5000"
		});
	};

  const rowClassName = (rowData) => {
    let id = rowData.dataRow.filter(r => Object.keys(r.fieldData)[0] === "id")[0].fieldData.id;
    console.log(rowData.dataRow, props.idSelectedRow)
    return {'p-highlight' : (id === props.idSelectedRow)};
  }
  
	let totalCount = <span>Total: {totalRecords} rows</span>;

	return (
		<div>
			<ButtonsBar
				buttons={props.customButtons ? props.customButtons : customButtons}
			/>
			{/* <Toolbar>
                <CustomButton label="Visibility" icon="6" />                
                <CustomButton label="Filter" icon="7" />   
                <CustomButton label="Group by" icon="8" />   
                <CustomButton label="Sort" icon="9" />   
            </Toolbar> */}
			<div className={styles.Table}>
				<DataTable
					value={fetchedData}
					paginatorRight={totalCount}
					resizableColumns={true}
					reorderableColumns={true}
					paginator={true}
					rows={numRows}
					first={firstRow}
					onPage={onChangePageHandler}
					rowsPerPageOptions={[5, 10, 20, 100]}
					lazy={true}
					loading={loading}
					totalRecords={totalRecords}
					sortable={true}
					onSort={onSortHandler}
					header={header}
					sortField={sortField}
					sortOrder={sortOrder}
          autoLayout={true}
          rowClassName={rowClassName}
				>
					{columns}
				</DataTable>
			</div>
			<Growl ref={growlRef} />
			<Dialog
				header={resources.messages["uploadDataset"]}
				visible={importDialogVisible}
				className={styles.Dialog}
				dismissableMask={false}
				onHide={onHideHandler}
			>
				<CustomFileUpload
					mode="advanced"
					name="file"
					url={`${window.env.REACT_APP_BACKEND}/dataset/${dataSetId}/loadTableData/${props.id}`}
					onUpload={onUploadHandler}
					multiple={false}
					chooseLabel={resources.messages["selectFile"]} //allowTypes="/(\.|\/)(csv|doc)$/"
					fileLimit={1}
					className={styles.FileUpload}
				/>
			</Dialog>

			<ReporterDataSetContext.Provider>
				<ConfirmDialog
					onConfirm={onConfirmDeleteHandler}
					onHide={() => setVisibleHandler(setDeleteDialogVisible, false)}
					visible={deleteDialogVisible}
					header={resources.messages["deleteDatasetTableHeader"]}
					maximizable={false}
					labelConfirm={resources.messages["yes"]}
					labelCancel={resources.messages["no"]}
				>
					{resources.messages["deleteDatasetTableConfirm"]}
				</ConfirmDialog>
			</ReporterDataSetContext.Provider>
			<SnapshotSlideBar
				isVisible={snapshotIsVisible}
				setIsVisible={setSnapshotIsVisible}
			/>
		</div>
	);
};

export default  withRouter(React.memo(DataViewer));
