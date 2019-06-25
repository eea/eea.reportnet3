import React from "react";

const IconComponent = props => {
	const { icon } = props;
	return <i className={`pi ${icon}`} />;
};

export default IconComponent;
