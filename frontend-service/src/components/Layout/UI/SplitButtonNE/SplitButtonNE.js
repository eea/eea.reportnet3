import React from "react";
import { SplitButton } from "primereact/splitbutton";

export class SplitButtonNE extends SplitButton {
	constructor(props) {
		super(props);
		this.state = {
			items: [
				{
					label: "Release to data collection",
					icon: "pi pi-unlock",
					command: e => {
						this.growl.show({
							severity: "success",
							summary: "Updated",
							detail: "Data Updated"
						});
					}
				},
				{
					label: "Import from file",
					icon: "pi pi-upload",
					command: e => {
						this.growl.show({
							severity: "success",
							summary: "Delete",
							detail: "Data Deleted"
						});
					}
				},
				{
					label: "Duplicate",
					icon: "pi pi-copy",
					command: e => {
						window.location.href = "https://facebook.github.io/react/";
					}
				},
				{
					label: "Properties",
					icon: "pi pi-info-circle",
					command: e => {
						window.location.hash = "/fileupload";
					}
				}
			]
		};

		this.save = this.save.bind(this);
	}

	save() {
		this.props.handleRedirect("/reporter-data-set");
	}

	render() {
		return (
			<SplitButton
				label="NE"
				icon=""
				onClick={this.save}
				model={this.state.items}
			/>
		);
	}
}
