const getUrl = (url, params = {}) => {
	let cUrl = url;
	if (params instanceof Object) {
		const keys = Object.keys(params);
		keys.forEach(key => {
			cUrl = cUrl.replace(`{:${key}}`, params[key]);
		});
	} else {
		params.forEach(param => {
			cUrl = cUrl.replace("{?}", param);
		});
	}
	return cUrl;
};

export default getUrl;
