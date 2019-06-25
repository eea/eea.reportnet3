import React from "react";

const IconComponent = props => {
	const { icon } = props;
	return <i className={`${icon}`} />;
};

export default IconComponent;
