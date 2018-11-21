var typeMap;
function entryRenderer(key, value) {
	if (!typeMap) {
		var entrys = $("#entrys").val();
		if (entrys) {
			typeMap = eval("(" + entrys + ")");
		} else {
			return function(value) {
				return value;
			};
		}
	}
	var entryMap = typeMap[key];
	if (entryMap != null && entryMap[value] != null) {
		return entryMap[value][lang];
	} else {
		return '';
	}
}
function entryRendererMulti(key, value) {
	if (!typeMap) {
		var entrys = $("#entrys").val();
		if (entrys) {
			typeMap = eval("(" + entrys + ")");
		} else {
			return function(value) {
				return value;
			};
		}
	}
	var entryMap = typeMap[key];
	var entryValues = "";
	if (value != null && value != "") {
		var values = value.split(",");
		for (var i = 0; i < values.length; i++) {
			if (entryMap != null && entryMap[values[i]] != null) {
				entryValues=entryValues!=""?entryValues+",":entryValues;
				entryValues+=entryMap[values[i]][lang];
			}
		}
	}
	return entryValues;
}

function keysValue(arrays,value){
	if(value){
		var vals = value.split(',');
		var srcs = "";
		for(var i in vals){
			for(var j in arrays){
				if(vals[i]== arrays[j].code){
					srcs += arrays[j].name +",";
					break;
				}
			}
		}
		if(srcs){
			return srcs.substring(0,srcs.length-1);
		}
	}
}

function searchSubmit(tableId, searchColumns, url, callback) {
	var param = "{'offset':0,";
	for (var i = 0; i < searchColumns.length; i++) {
		var element = $(searchColumns[i]);
		if(!element.attr("name")){
			continue;
		}
		var value = textValid(element.val());
		if (value) {
			param += "'queryCondition[" + element.attr("name") + "]':'" + value
					+ "',";
		}
	}
	if (param.length > 1) {
		param = param.substring(0, param.length - 1);
		param += "}";
		if (param.length > 13) {
			$('#queryParams_' + tableId).html("{" + param.substring(12));
		} else {
			$('#queryParams_' + tableId).html('');
		}
		//$('#' + tableId).bootstrapTable('selectPage', 1);
		var refreshParam = {
			query : eval('(' + param + ')')
		}
		
		if(callback){
			ajaxRequest(url,refreshParam,function(result){
				window[callback](result.data);;
			},false);
		}else{
			bootStrapTableRefresh(tableId, url, refreshParam);
		}
	}
}

function bootStrapTableRefresh(tableId, url, refreshParam) {
	if (url) {
		refreshParam.url = url;
	}
	var tables=tableId.split(',');
	for (var i = 0; i < tables.length; i++) {
		var table = $('#'+tables[i]);
		table.bootstrapTable('refresh', refreshParam);
	}
}

function downAjax(title,url,tableId){
	var param=$('#queryParams_' + tableId).html();
	if(param){
		param = param.replace(/(^\s*)|(\s*$)/g,'');
	}
	
	if(title.indexOf("->")>-1){
		title = title.substring(title.lastIndexOf("->")+2);
	}
	title = window.encodeURI(window.encodeURI(title));
	title = url.indexOf("?")>-1?"&downName="+title:"?downName="+title;
	url += title; 
	var grid=$('#'+tableId);
	var rows = getSelectedRows(tableId);
	if (rows.length > 0) {
		var ids = "";
		for ( var i = 0; i < rows.length; i++) {
			ids += rows[i].id +",";
		}
		if(ids != ""){
			ids=ids.substring(0, ids.length-1);
		}
		url += "&ids="+ids;
	}
	
	if (param&&param.length > 1) {
		param = eval('(' + param + ')');
	}else {
		param = {};
	}
	$.ajax({
		url:url,
		type:"GET",
		data:param,
		success:function(){
			infoTip("导出成功!");
		}
	});
	infoTip("正在导出请稍等到导出列表下载!");
}

function down(title,url,tableId){
	var param=$('#queryParams_' + tableId).html();
	if(param){
		param = param.replace(/(^\s*)|(\s*$)/g,'');
	}
	
	if(title.indexOf("->")>-1){
		title = title.substring(title.lastIndexOf("->")+2);
	}
	title = window.encodeURI(window.encodeURI(title));
	title = url.indexOf("?")>-1?"&downName="+title:"?downName="+title;
	url += title; 
	var grid=$('#'+tableId);
	var rows = getSelectedRows(tableId);
	if (rows.length > 0) {
		var ids = "";
		for ( var i = 0; i < rows.length; i++) {
			ids += rows[i].id +",";
		}
		if(ids != ""){
			ids=ids.substring(0, ids.length-1);
		}
		url += "&ids="+ids;
	}
	if (param&&param.length > 1) {
		//param=param.substring(0, param.length-1);
		//param+="}";
		Highcharts.post(url, eval('(' + param + ')'));
	}else {
		Highcharts.post(url, {});
	}
}

function downAttachment(url, params){
	Highcharts.post(url, params);
}

function refreshTable(tableId, searchBtnObj, url, callback) {
	var searchColumns = $(searchBtnObj).parents(".searchColumns").find(
			"input,select");
	searchSubmit(tableId, searchColumns, url, callback);
}
function textValid(text) {
	if(text == null){
		return text;
	}
	if(text instanceof Array){
		text = JSON.stringify(text);
	}
	return text.replace(/'/g, "");
}
function resetSearch() {
	$(".searchColumns select").find("option").removeAttr("selected");
	$(".searchColumns .multiselect-native-select select").each(function(index, element){
		$("#"+element.id).multiselect('deselectAll', false);
		$("#"+element.id).multiselect('updateButtonText');
		$("#"+element.id).multiselect('refresh');
	});
	$(".searchColumns input,.searchColumns select,.searchColumns textarea").not('.searchColumns .multiselect-native-select select').not('.searchColumns .multiselect-native-select input')
	.val('');
}

function indexFormatter(value, row, index) {
	return index + 1;
}

function btableQueryParams(params) {
	var queryParamsStr = $.trim($("#queryParams_" + this.id).html());
	if (queryParamsStr.length > 0) {
		var queryParams = eval('(' + queryParamsStr + ')');
		for ( var attrname in queryParams) {
			params[attrname] = queryParams[attrname];
		}
	}
	return params;
}
function add(title, addurl, gridId) {
	createwindow(title, addurl, gridId);
}

function importFile(title, importUrl, gridId, options) {
	importUrl = importUrl.replace('&','%26');
	var url=$("#baseUrl").val() + "/system/upload/importFileInput.do?importUrl="+importUrl;
	
	var defaultOptions = {};
	var opts = $.extend(defaultOptions, options);
	
	if(opts.templateUrl) {
		url += "&templateUrl="+options.templateUrl;
	}
	if(opts.allowedFileExtensions) {
		url += "&fileTypeDesc="+options.allowedFileExtensions;
	}
	if(opts.callback){
		url += "&callback="+options.callback;
	}
	createwindow(title, url, gridId);
}
function importImage(title, importUrl, gridId, options) {
	importUrl = importUrl.replace('&','%26');
	var url=$("#baseUrl").val() + "/system/upload/importImageInput.do?importUrl="+importUrl;
	
	var defaultOptions = {};
	var opts = $.extend(defaultOptions, options);
	
	if(opts.allowedFileExtensions) {
		url += "&fileTypeDesc="+options.allowedFileExtensions;
	}
	if(opts.callback){
		url += "&callback="+options.callback;
	}
	createwindow(title, url, gridId);
}

function update(title, url, gridId) {
	var rowsData = getSelectedRows(gridId);
	if (!rowsData || rowsData.length == 0) {
		infoTip(commons_msg.plsSelData);
		return;
	}
	if (rowsData.length > 1) {
		infoTip(commons_msg.plsSelOne);
		return;
	}
	if (url.indexOf('?') != -1) {
		url += '&id=' + rowsData[0].id;
	} else {
		url += '?id=' + rowsData[0].id;
	}

	createwindow(title, url, gridId);
}
function frame_Select(options) {
	if(options.url instanceof Function){
		options.url = options.url.call();
	}
	options.url = options.url.replace(' ','%20');
	BootstrapDialog.show({
		size : BootstrapDialog.SIZE_WIDE,
		title : options.title,
		closeByBackdrop: false,
		draggable: true,
		message : function(dialog) {
			var $message = $('<div></div>');
			var pageToLoad = dialog.getData('pageToLoad');
			$message.load(pageToLoad);

			return $message;
		},
		buttons : [ {
			label : commons_msg.btnConfirm,
			cssClass : 'btn-primary',
			action : function(dialogRef) {
				options.callback(dialogRef);
			}
		}, {
			label : commons_msg.btnClose,
			cssClass : 'btn-default',
			action : function(dialogRef) {
				dialogRef.close();
			}
		} ],
		data : {
			'pageToLoad' : options.url
		}
	});
}


function frame_cleanSelect(target) {
	var input = $(target).parents(".value").find("input");
	if (input.length < 1) {
		input = $(target).prevAll("input");
	}
	for (var i = 0; i < input.length; i++) {
		$(input[i]).val('');
	}
}
function detail(title, url, gridId) {
	var rowsData = getSelectedRows(gridId);
	if (!rowsData || rowsData.length == 0) {
		infoTip(commons_msg.plsSelData);
		return;
	}
	if (rowsData.length > 1) {
		infoTip(commons_msg.plsSelOne);
		return;
	}
	if (url.indexOf('?') != -1) {
		url += '&id=' + rowsData[0].id;
	} else {
		url += '?id=' + rowsData[0].id;
	}
	createdetailwindow(title, url, gridId);
}

function createdetailwindow(title, url, gridId) {
	BootstrapDialog.show({
		size : BootstrapDialog.SIZE_WIDE,
		title : title,
		closeByBackdrop: false,
		draggable: true,
		message : function(dialog) {
			var $message = $('<div></div>');
			var pageToLoad = dialog.getData('pageToLoad');
			$message.load(pageToLoad);

			return $message;
		},
		buttons : [ {
			/*label : commons_msg.btnPrint,
			cssClass : 'btn-default',
			action : function(dialogRef) {
				printer(1)
			}
		},{*/
			label : commons_msg.btnClose,
			cssClass : 'btn-primary',
			action : function(dialogRef) {
				closeDialog(dialogRef)
			}
		}],
		data : {
			'pageToLoad' : url,
			'gridId' : gridId
		},
		 onhide: function(dialogRef){
			 var grid = $('#' + dialogRef.getData('gridId'));
			 if (grid) {
				grid.bootstrapTable('refresh');
			 }
         }
	});

}
function createScrapwindow(title, url, gridId) {
	var rows = getSelectedRows(gridId);
	if (rows.length > 0) {
		var ids = [];
		BootstrapDialog
				.confirm({
					title : commons_msg.scrap,
					message : commons_msg.scrapMessage,
					closable : true, 
					draggable : true, 
					btnCancelLabel : commons_msg.btnCancel, 
					btnOKLabel : commons_msg.btnConfirm,
					callback : function(result) {
						if (result) {
							BootstrapDialog.show({
								size : BootstrapDialog.SIZE_WIDE,
								title : title,
								closeByBackdrop: false,
								draggable: true,
								message : function(dialog) {
									var $message = $('<div></div>');
									var pageToLoad = dialog.getData('pageToLoad');
									$message.load(pageToLoad);

									return $message;
								},
								buttons : [ {
									label : commons_msg.btnClose,
									cssClass : 'btn-default',
									action : function(dialogRef) {
										closeDialog(dialogRef)
									}
								} ],
								data : {
									'pageToLoad' : url,
									'gridId' : gridId
								},
								 onhide: function(dialogRef){
									 var grid = $('#' + dialogRef.getData('gridId'));
									 if (grid) {
										grid.bootstrapTable('refresh');
									 }
						         }
							});
						}
					}
				});
	} else {
		infoTip(commons_msg.scrapInfo);
	}

}
function createChildwindow(title, url, gridId) {
	BootstrapDialog.show({
		title : title,
		size : BootstrapDialog.SIZE_WIDE,
		closeByBackdrop: false,
		draggable: true,
		message : function(dialog) {
			var $message = $('<div></div>');
			var pageToLoad = dialog.getData('pageToLoad');
			$message.load(pageToLoad);

			return $message;
		},
		buttons : [ {
			label : commons_msg.btnClose,
			cssClass : 'btn-default',
			action : function(dialogRef) {
				closeDialog(dialogRef)
				var grid = $('#' + dialogRef.getData('gridId'));
				if (grid) {
					grid.bootstrapTable('refresh');
				}
			}
		} ],
		data : {
			'pageToLoad' : url,
			'gridId' : gridId
		},
		 onhide: function(dialogRef){
			 var grid = $('#' + dialogRef.getData('gridId'));
			 if (grid) {
				grid.bootstrapTable('refresh');
			 }
         }
	});

}

function createwindow(title, url, gridId, onshown,submit) {
	BootstrapDialog.show({
		size : BootstrapDialog.SIZE_WIDE,
		title : title,
		closeByBackdrop: false,
		draggable: true,
		message : function(dialog) {
			var $message = $('<div></div>');
			var pageToLoad = dialog.getData('pageToLoad');
			$message.load(pageToLoad);
			return $message;
		},
		onshown : function(dialog) {
			if(onshown) {
				onshown(dialog);
			}
		},
		buttons : [ {
			label : commons_msg.btnConfirm,
			cssClass : 'btn-primary',
			action : function(dialogRef) {
				if (typeof submit != "undefined" && submit) {
					window[submit](dialogRef);
				}else{
					submitForm(dialogRef);
				}
			}
		}, {
			label : commons_msg.btnClose,
			cssClass : 'btn-default',
			action : function(dialogRef) {
				closeDialog(dialogRef)
			}
		}],
		data : {
			'pageToLoad' : url,
			'gridId' : gridId
		}
	});

}
/**
 * 执行完确定按钮事件后,回调传进来的方法
 * @param title
 * @param url
 * @param callback 方法名
 * @param param  回调方法需要的参数
 */
function createwindowCallBack(title, url, callback,param) {
	BootstrapDialog.show({
		size : BootstrapDialog.SIZE_WIDE,
		title : title,
		closeByBackdrop: false,
		draggable: true,
		message : function(dialog) {
			var $message = $('<div></div>');
			var pageToLoad = dialog.getData('pageToLoad');
			$message.load(pageToLoad);
			return $message;
		},
		buttons : [ {
			label : commons_msg.btnConfirm,
			cssClass : 'btn-primary',
			action : function(dialogRef) {
				submitForm(dialogRef);
				if(typeof callback == 'function'){
					callback(param);
				}
			}
		}, {
			label : commons_msg.btnClose,
			cssClass : 'btn-default',
			action : function(dialogRef) {
				closeDialog(dialogRef)
			}
		}],
		data : {
			'pageToLoad' : url
		}
	});

}
function submitForm(dialogRef) {
	$('#'+dialogRef.getId()).find("input[id='btn_sub']").click();
}

function closeOutterDialog() {
	var dialogZIndex = -1;
	var dialogInstanceTemp = null;
	$.each(BootstrapDialog.dialogs, function(dialogId, dialogInstance) {
		if(dialogInstance.getData('gridId')){
			var tables=dialogInstance.getData('gridId').split(',');
			for (var i = 0; i < tables.length; i++) {
				var table = $('#'+tables[i]);
				if (table) {
					table.bootstrapTable('refresh');
				}
			}
		}
		if ($("#" + dialogId).css("z-index") * 1 >= dialogZIndex) {
			dialogZIndex = $("#" + dialogId).css("z-index") * 1;
			dialogInstanceTemp = dialogInstance;
		}
	});
	if (dialogInstanceTemp) {
		closeDialog(dialogInstanceTemp);
	}
}

function closeDialog(dialogRef) {
	$('#'+dialogRef.getId()).find("input[id='callback']").detach();
	$('#'+dialogRef.getId()).find("input[id='beforeSubmit']").detach();
	dialogRef.close();
}
function frame_grid_remove(url, gridId) {
	remove(url, gridId);
}
function remove(url, gridId) {
	var rows = getSelectedRows(gridId);
	if (rows.length > 0) {
		var ids = [];
		BootstrapDialog
				.confirm({
					title : commons_msg.btnDelete,
					message : commons_msg.deleteConfirm,
					// type: BootstrapDialog.TYPE_WARNING, // <-- Default value
					// is BootstrapDialog.TYPE_PRIMARY
					closable : true, // <-- Default value is false
					draggable : true, // <-- Default value is false
					btnCancelLabel : commons_msg.btnCancel, // <-- Default value is 'Cancel',
					btnOKLabel : commons_msg.btnConfirm, // <-- Default value is 'OK',
					// btnOKClass: 'btn-warning', // <-- If you didn't specify
					// it, dialog type will be used,
					callback : function(result) {
						if (result) {
							var refCount = 0;
							var sysDataCount = 0;
							for (var i = 0; i < rows.length; i++) {
								if (rows[i].id != null) {
									if (rows[i].refCount
											&& rows[i].refCount > 0) {
										refCount++;
									}
									if (rows[i].systemData
											&& rows[i].systemData == 'Y') {
										sysDataCount++;
									}
									ids.push(rows[i].id);
								}
							}
							if (sysDataCount > 0) {
								infoTip(commons_msg.noDeleteSysData);
							} else if (refCount > 0) {
								infoTip(commons_msg.noDeleteQuoteData);
							} else {
								ajaxRequest(url, {
									'ids' : JSON.stringify(ids)
								}, function() {
									var currentPage = 1;
									var totalPage = 1;
									var deleteRows = ids.length;
									var currentPageRows = $('#' + gridId).bootstrapTable('getData', true).length;
									totalPage = $(".pagination").find('.page-number').length;
									currentPage = $(".pagination .page-number.active a").html() * 1;
									if (currentPage > 1 && currentPage == totalPage && deleteRows == currentPageRows) {
										$('#' + gridId).bootstrapTable('prevPage');
									} else {
										reloadTable(gridId);
									}
								});
							}
						}
	          }
		});
	} else {
		infoTip(commons_msg.plsSelDeleteData);
	}
}
function ajaxRequestSync(url,data,callback,showSuccess){
	data = data ? data : {};
	showSuccess = showSuccess != null ? showSuccess : true;
	$.ajax({
		url:url,
		type:"post",
		data:data,
		dataType:"json",
		async:false,
		success:function(result){
			if (result.success) {
				if (showSuccess) {
					infoTip(commons_msg.handleSuccess);
				}
				if (callback) {
					callback(result);
				}
			} else {
				errorTip(result.message);
				if (callback) {
					callback(result);
				}
			}
		}
	});
}
function ajaxRequest(url, data, callback, showSuccess , showErrorTip) {
	data = data ? data : {};
	showSuccess = showSuccess != null ? showSuccess : true;
	$.post(url, data, function(result) {
		if (result.success) {
			if (showSuccess) {
				infoTip(commons_msg.handleSuccess);
			}
			if (callback) {
				callback(result);
			}
		} else {
			if(!showErrorTip){
				errorTip(result.message);
			}
			if (callback) {
				callback(result);
			}
		}
	});
}
function tip(title, message, iconUrl, sticky) {
	return $.gritter.add({
		title : title,
		text : message,
		image : iconUrl,
		sticky : sticky,
		time : 4000,
		speed : 500,
		position : 'bottom-right',
		class_name : 'gritter-info'// gritter-info  gritter-center gritter-success
	});
}
function warningTip(message) {
	tip(commons_msg.errorTip, message, $("#baseUrl").val() + '/res/bootstrap/css/images/button-error-01.png', false);
}
function infoTip(message) {
	tip(commons_msg.tip, message, $("#baseUrl").val() + '/res/bootstrap/css/images/button-info-01.png', false);
}
var errorTipId;
function errorTip(message) {
	if(errorTipId){
		$.gritter.remove(errorTipId, {
			fade: true,
			speed: 'slow'
		});
	}
	errorTipId = tip(commons_msg.errorTip, message, $("#baseUrl").val() + '/res/bootstrap/css/images/button-error-01.png', false);
}
function checkForFileinputRequired(curform) {
	var flag = true;
	$(curform).find("input[type='file']").each(function(index, element) {
		if ($(element).attr("required") && !$(element).attr("ignore")) {
			if ($(element).val() == "") {
				flag = false;
				$(element).focus();
				infoTip(commons_msg.plsSelFile);
				return false;
			}
		}
	});
	return flag;
}
function checkFileSubmit(curform) {
	var fileFlag = false;
	$(curform).find("[type='file']").each(function(index, element) {
		if ($(element).val() != "") {
			fileFlag = true;
		} else {
			$(element).detach();
		}
	});
	if (fileFlag) {
		spinner.spin($("#btn_sub").parent("form")[0]);
		curform.ajaxSubmit({
			type : 'post',
			success : function(data) {
				initValidFormCallback(data);
			}
		});
		return false;
	} else {
		return true;
	}
}
function initValidForm(beforeCheck, beforeSubmit, callback) {
	$("form")
			.Validform(
					{
						tiptype : function(msg,o,cssctl){
							if (o.type == 3) {
								infoTip(msg);
							}
						},
						btnSubmit : "#btn_sub",
						ajaxPost : true,
						beforeCheck : function(curform) {
							if (typeof beforeCheck != "undefined" && beforeCheck != "") {
								return window[beforeCheck](curform);
							}
						},
						beforeSubmit : function(curform) {
							if (!checkForFileinputRequired(curform)) {
								return false;
							}
							var result = true;
							if (typeof beforeSubmit != "undefined" && beforeSubmit != "") {
								result = window[beforeSubmit](curform);
							}
							if (result) {
								result = checkFileSubmit(curform);
							}
							if(result) {
								spinner.spin($("#btn_sub").parent("form")[0]);
							}
							return result;
						},
						callback : function(result){
							initValidFormCallback(result, callback);
						}
					});
}

function initValidFormCallback(result, callback) {
	if (result.success) {
		closeOutterDialog();
		if(result.message){
			infoTip(result.message);
		}else{
			infoTip(commons_msg.handleSuccess);
		}
		if (callback) {
			window[callback](result);
		}
	} else {
		errorTip(result.message);
	}
	spinner.spin();
}

/**
 * 判断字符串是否以另一个字符串开头
 * 
 * @param str
 * @param sub
 * @returns {Boolean}
 */
function startWith(str, sub) {
	if (undefined == str || undefined == sub) {
		return false;
	}
	str += "";
	sub += "";
	if (sub.length == 0 || str.length < sub.length) {
		return false;
	}
	return str.substring(0, sub.length) == sub;
}

function getSelectedRows(gridId) {
	var grid = $('#' + gridId);
	return grid.bootstrapTable('getSelections');
}

function getSelectedIds(gridId) {
	var rows = getSelectedRows(gridId);
	var ids = [];
	for (var i = 0; i < rows.length; i++) {
		if (rows[i].id != null) {
			ids.push(rows[i].id);
		}
	}
	return ids;
}

function reloadTable(gridId) {
	var grid = $('#' + gridId);
	grid.bootstrapTable('refresh');
}

function initFileInput(ctrlName, options) {
	var defaultOptions={
		language: 'zh',// 设置语言
		// uploadUrl : uploadUrl, // 上传的地址
		// deleteUrl : deleteUrl,// 删除的地址
		// showPreview:false,
		// showRemove: false,
		allowedPreviewTypes : ['image'],
		uploadAsync : false,
		maxFileCount : 0,
		maxFileSize : 10240,
		validateInitialCount : true
		//allowedFileExtensions : [ 'xls', 'xlsx' ]
	};
	var opts = $.extend(defaultOptions, options);
	if(opts.callback){
		$('#' + ctrlName).on('filebatchuploadsuccess', opts.callback);
	}
	$('#' + ctrlName).fileinput(opts);
}

/**
 * 加载导航新通知信息
 */
function loadNotificationCountInfo() {
	$
			.ajax({
				url : $("#baseUrl").val() + "/notification/countInfo.do",
				cache : false,
				global : false,
				type : "POST",
				data : {},
				dataType : "json",
				async : true,
				success : function(data) {
					if (data != null && data.success && data.totalCount >= 0) {
						$("#notifiction_count").html(data.totalCount);
						var notificationsData = data.notifications;
						var notifications = '';
						notifications += '<li class="dropdown-header">';
						notifications += '	<i class="icon-warning-sign"></i>'
								+ data.totalCount + commons_msg.notificationCount;
						notifications += '</li>';
						if (notificationsData && notificationsData.length > 0) {
							for (var i = 0; i < notificationsData.length; i++) {
								var notification = notificationsData[i];
								notifications += '<li>';
								notifications += '<a onclick="openChildMenu(90010)" href="javascript:void(0);">';
								notifications += '<div class="clearfix">';
								notifications += '<span class="pull-left">';
								notifications += '	<i class="btn btn-xs no-hover btn-pink icon-comment"></i>'
										+ renderMsgtype(notification.msgType);
								notifications += '</span>';
								notifications += '<span class="pull-right badge badge-info">+'
										+ notification.num + '</span>';
								notifications += '</div>';
								notifications += '</a>';
								notifications += '</li>';
							}
						}

						notifications += '<li>';
						notifications += '	<a onclick="openChildMenu(90010)" href="avascript:void(0);">'+ commons_msg.viewAllNoti + '<i class="icon-arrow-right"></i></a>';
						notifications += '</li>';
						$("#ul_notification_box").html(notifications);
						if ($("#unread_notification_count")) {
							$("#unread_notification_count").html(
									$("#notifiction_count").html());
						}
					}
				}
			});
}

/**
 * 质量问题调查报告模板
 * */
function createReportWindow(title, url,gridId) {
	BootstrapDialog.show({
		size : BootstrapDialog.SIZE_WIDE,
		title : title,
		closeByBackdrop: false,
		draggable: true,
		message : function(dialog) {
			var $message = $('<div></div>');
			var pageToLoad = dialog.getData('pageToLoad');
			$message.load(pageToLoad);

			return $message;
		},
		buttons : [ {
			label : commons_msg.btnSave,
			cssClass : 'btn-primary',
			action : function(dialogRef) {
				submitForm(dialogRef);
			}
		}, {
			label : commons_msg.btnUploadDown,
			cssClass : 'btn-primary',
			action : function(dialogRef) {
				var theform = $("form");
				if (document.getElementById("uploadAndDown")) {
					$("#uploadAndDown").val('true');
				} else {
					$("<input type='hidden' id='uploadAndDown' name='uploadAndDown' value='true'>").appendTo(theform);
				}
				submitForm(dialogRef);
			}
		} , {
			label : commons_msg.btnClose,
			cssClass : 'btn-default',
			action : function(dialogRef) {
				closeDialog(dialogRef)
			}
		} ],
		data : {
			'pageToLoad' : url += '&gridId=' + gridId,
		}
	});
}

/**
 * 在必填项之前加*标识
 */
function initFormRequiredFlag() {
	$("span.form_required_flag").detach();
	$(".form-control,input[type='radio'],input[type='checkbox']").each(function(index, element) {
		if (($(element).attr("datatype") || $(element).attr("required")) && !$(element).attr("ignore")) {
			$(element).before('<span class="form_required_flag">*</span>');
		}
	});
	$("input[type='file']").each(function(index, element) {
		if (($(element).attr("datatype") || $(element).attr("required")) && !$(element).attr("ignore")) {
			$(element).parents(".file_input_box").before('<span class="form_required_flag">*</span>');
		}
	});
}
function printer(oper) {
	var baseUrl = $('#baseUrl').val();
	var bdhtml = window.document.body.innerHTML;// 获取当前页的html代码
	if (oper < 10) {
		var dialogZIndex = -1;
		var dialogInstanceTemp = null;
		$.each(BootstrapDialog.dialogs, function(dialogId, dialogInstance) {
			if ($("#" + dialogId).css("z-index") * 1 >= dialogZIndex) {
				dialogZIndex = $("#" + dialogId).css("z-index") * 1;
				dialogInstanceTemp = dialogInstance;
			}
		});
		if (dialogInstanceTemp) {
			var form = $('#'+dialogInstanceTemp.getId()).find("div[class='bootstrap-dialog-message']");
			if(form.length > 0) {
				bdhtml = form[0].innerHTML;
			}
		}

//		var sprnstr = "<!--startprint" + oper + "-->";// 设置打印开始区域
//		var eprnstr = "<!--endprint" + oper + "-->";// 设置打印结束区域
//		var prnhtml = bdhtml.substring(bdhtml.indexOf(sprnstr) + 18); // 从开始代码向后取html
//
//		prnhtml = prnhtml.substring(0, prnhtml.indexOf(eprnstr));// 从结束代码向前取html
	}

	var OpenWindow = window.open("");
	OpenWindow.document.write("<HTML>");
	OpenWindow.document.write("<HEAD>");
	OpenWindow.document
			.write("<meta http-equiv=\"Content-Type\" content=\"text\/html; charset=utf-8\" \/>");
	OpenWindow.document.write("<TITLE>PrintPage<\/TITLE>");

	// 这里写你的CSS地址
	OpenWindow.document.write("<link rel=\"stylesheet\" href=\"");
	OpenWindow.document.write(baseUrl);
	OpenWindow.document.write("/res/bootstrap/css/bootstrap.min.css\">");

	OpenWindow.document.write("<link rel=\"stylesheet\"  href=\"");
	OpenWindow.document.write(baseUrl);
	OpenWindow.document.write("/res/bootstrap/css/bootstrap-theme.min.css\">");

	OpenWindow.document.write("<link rel=\"stylesheet\"  href=\"");
	OpenWindow.document.write(baseUrl);
	OpenWindow.document.write("/res/bootstrap/css/font-awesome.min.css\">");

	OpenWindow.document.write("<link rel=\"stylesheet\" href=\"");
	OpenWindow.document.write(baseUrl);
	OpenWindow.document.write("/res/bootstraptable/result-light.css\">");

	OpenWindow.document.write("<link rel=\"stylesheet\" href=\"");
	OpenWindow.document.write(baseUrl);
	OpenWindow.document.write("/res/bootstraptable/bootstrap-table.css\">");

	OpenWindow.document.write("<link rel=\"stylesheet\" href=\"");
	OpenWindow.document.write(baseUrl);
	OpenWindow.document
			.write("/res/bootstrap/css/jquery-ui-1.10.3.full.min.css\">");

	OpenWindow.document.write("<link rel=\"stylesheet\" href=\"");
	OpenWindow.document.write(baseUrl);
	OpenWindow.document.write("/res/Validform/css/style.css\">");

	OpenWindow.document.write("<link rel=\"stylesheet\" href=\"");
	OpenWindow.document.write(baseUrl);
	OpenWindow.document.write("/res/fileinput-4.3.1/css/fileinput.min.css\">");
	OpenWindow.document.write("<link rel=\"stylesheet\" href=\"");
	OpenWindow.document.write(baseUrl);
	OpenWindow.document.write("/res/bootstrap/css/ace.min.css\">");
	OpenWindow.document.write("<link rel=\"stylesheet\" href=\"");
	OpenWindow.document.write(baseUrl);
	OpenWindow.document.write("/res/bootstrap/css/ace-rtl.min.css\">");
	OpenWindow.document.write("<link rel=\"stylesheet\" href=\"");
	OpenWindow.document.write(baseUrl);
	OpenWindow.document.write("/res/bootstrap/css/ace-skins.min.css\">");
	OpenWindow.document.write("<link rel=\"stylesheet\" href=\"");
	OpenWindow.document.write(baseUrl);
	OpenWindow.document.write("/res/css/wis.css\">");
	OpenWindow.document.write("<link rel=\"stylesheet\" href=\"");
	OpenWindow.document.write(baseUrl);
	OpenWindow.document.write("/res/css/processPage.css\">");

	OpenWindow.document.write("<\/HEAD>");
	OpenWindow.document.write("<BODY class=\"form-horizontal\">");

	OpenWindow.document.write("<\/BODY>");
	OpenWindow.document.write("<\/HTML>");

	bdhtml = bdhtml.replace(/col-md/g, "col-xs");
	OpenWindow.document.body.innerHTML = bdhtml;
	OpenWindow.document.close();
	OpenWindow.print();
}
var spinOpts = {  
		  lines: 13, // 画的线条数
		  length: 7, // 每条线的长度
		  width: 5, // 线宽
		  radius: 20, // 线的圆角半径
		  corners: 1, // Corner roundness (0..1)
		  rotate: 0, // 旋转偏移量
		  direction: 1, // 1: 顺时针, -1: 逆时针
		  color: '#000', // #rgb or #rrggbb or array of colors   
		  speed: 1, // 转速/秒   
		  trail: 60, // Afterglow percentage   
		  shadow: false, // 是否显示阴影   
		  hwaccel: false, // 是否使用硬件加速   
		 // className: 'spinner', // 绑定到spinner上的类名   
		  zIndex: 2e9, // 定位层 (默认 2000000000)   
		  top: 'auto', // 相对父元素上定位，单位px   
		  left: 'auto' // 相对父元素左定位，单位px   
		};
var spinner = new Spinner(spinOpts);

function compare(value1, value2) {
	if (value1 < value2) {
		return -1;
	} else if (value1 > value2) {
		return 1;
	} else {
		return 0;
	}
}
function fixWidth(value, row, index, field) {
	return {
		classes : 'text-nowrap fixWidth',
		css : {
			"font-size" : "13px"
		}
	};
}
function loadTabView(url, async, callback) {
	$.ajax({
        url: url,
        cache: false,
        global: false,
        type: "GET",
        dataType: "html",
        async:async,
        data : {},
        success: function(html){
    		callback(html);
        }
    });
}
function magnifyCallback(dialog, callbackOptions) {
	var rowsData = getSelectedRows('dg'+callbackOptions.gridId);
	if (!rowsData || rowsData.length == 0) {
		infoTip(commons_msg.plsSelData);
		return;
	}
	$('#hidden_'+callbackOptions.fieldId).val(rowsData[0].id);
	$('#display_'+callbackOptions.fieldId).val(rowsData[0].name);
	dialog.close();
}

function createConfirmWindow(title,message,url, gridId) {
	var rows = getSelectedRows(gridId);
	if (rows.length > 0) {
		var ids = [];
		BootstrapDialog
				.confirm({
					title : title,
					message : message,
					// type: BootstrapDialog.TYPE_WARNING, // <-- Default value
					// is BootstrapDialog.TYPE_PRIMARY
					closable : true, // <-- Default value is false
					draggable : true, // <-- Default value is false
					btnCancelLabel : commons_msg.btnCancel, // <-- Default value is 'Cancel',
					btnOKLabel : commons_msg.btnConfirm, // <-- Default value is 'OK',
					// btnOKClass: 'btn-warning', // <-- If you didn't specify
					// it, dialog type will be used,
					callback : function(result) {
						if (result) {
							var refCount = 0;
							var sysDataCount = 0;
							for (var i = 0; i < rows.length; i++) {
								if (rows[i].id != null) {
									if (rows[i].refCount
											&& rows[i].refCount > 0) {
										refCount++;
									}
									if (rows[i].systemData
											&& rows[i].systemData == 'Y') {
										sysDataCount++;
									}
									ids.push(rows[i].id);
								}
							}
							if (sysDataCount > 0) {
								infoTip(commons_msg.noDeleteSysData);
							} else if (refCount > 0) {
								infoTip(commons_msg.noDeleteQuoteData);
							} else {
								ajaxRequest(url, {
									'ids' : JSON.stringify(ids)
								}, function() {
									var currentPage = 1;
									var totalPage = 1;
									var deleteRows = ids.length;
									var currentPageRows = $('#' + gridId).bootstrapTable('getData', true).length;
									totalPage = $(".pagination").find('.page-number').length;
									currentPage = $(".pagination .page-number.active a").html() * 1;
									if (currentPage > 1 && currentPage == totalPage && deleteRows == currentPageRows) {
										$('#' + gridId).bootstrapTable('prevPage');
									} else {
										reloadTable(gridId);
									}
								});
							}
						}
	          }
		});
	} else {
		infoTip(commons_msg.plsSelDeleteData);
	}
}


//推送消息公共方法(flag为true是表示推送)
function pushMessage(userId){
	var ws;
	var lockReconnect = false;//避免重复连接
	var wsUrl ="ws://192.168.90.12/mes/websocketWithSession/"+userId;
	function createWebSocket(url) {
	    try {
	        ws = new WebSocket(url);
	        initEventHandle();
	    } catch (e) {
	        reconnect(url);
	    }     
	}
	function initEventHandle() {
	    ws.onclose = function () {
	        reconnect(wsUrl);
	    };
	    ws.onerror = function () {
	        reconnect(wsUrl);
	    };
	    ws.onopen = function () {
	        //心跳检测重置
	        heartCheck.reset().start();
	    };
	    ws.onmessage = function (event) {
	    	var obj = event.data;
	        //如果获取到消息，心跳检测重置//拿到任何消息都说明当前连接是正常的
	    	if(obj && obj != 'HeartBeat'){
	    		infoTip(obj);
	    	}
	        heartCheck.reset().start();
	    }
	}
	function reconnect(url) {
	    if(lockReconnect) return;
	    lockReconnect = true;
	    //没连接上会一直重连，设置延迟避免请求过多
	    setTimeout(function () {
	        createWebSocket(url);
	        lockReconnect = false;
	    }, 2000);
	}
	//心跳检测
	var heartCheck = {
	    timeout: 60000,//60秒
	    timeoutObj: null,
	    reset: function(){
	        clearTimeout(this.timeoutObj);
	        return this;
	    },
	    start: function(){
	        this.timeoutObj = setTimeout(function(){
	            //这里发送一个心跳，后端收到后，返回一个心跳消息，
	            //onmessage拿到返回的心跳就说明连接正常
	            ws.send("HeartBeat");
	        }, this.timeout)
	    }
	}
	createWebSocket(wsUrl);
}

 function ajaxResponseHtml(url,parameters,tableId){
	 $.ajax({
	        url: url,
	        cache: false,
	        global: false,
	        type: "GET",
	        dataType: "html",
	        data :parameters,
	        success: function(html){
     		$("#"+tableId).html(html);
	        }
	    });
 }

 function wisConfirm(options){
	 BootstrapDialog
		.confirm({
			title : options.title?options.title:'提示',
			message : options.message?options.message:'',
			closable : true, 
			draggable : true, 
			btnCancelLabel : commons_msg.btnCancel, 
			btnOKLabel : commons_msg.btnConfirm, 
			callback :function(result) {
				if(options.callback){
					options.callback(result);
				}
			}
	});
 }
 function caseLessTen(data){
	if(data<10){
		return '0'+data;
	} 
	return data;
 }

function openSelector(options) {
    options.url = options.url.replace(' ','%20');
    BootstrapDialog.show({
        size : BootstrapDialog.SIZE_WIDE,
        title : options.title,
        closeByBackdrop: false,
        draggable: true,
        message : function(dialogRef) {
            var $message = $('<div></div>');
            var pageToLoad = dialogRef.getData('pageToLoad');
            $message.load(pageToLoad);
            return $message;
        },
        buttons : [  {
            label : commons_msg.btnConfirm,
            cssClass : 'btn-primary',
            action : function(dialogRef) {
                var ret = $('#'+dialogRef.getId()).find('[data-toggle="table"]').bootstrapTable('getSelections');
                options.callback((ret&&ret.length)?ret[0]:null);
                dialogRef.close();
            }
        } ],
        data : {
            'pageToLoad' : options.url
        }
    });
}

Date.prototype.format = function(format)
{
	 var o = {
	 "M+" : this.getMonth()+1, //month
	 "d+" : this.getDate(),    //day
	 "h+" : this.getHours(),   //hour
	 "m+" : this.getMinutes(), //minute
	 "s+" : this.getSeconds(), //second
	 "q+" : Math.floor((this.getMonth()+3)/3),  //quarter
	 "S" : this.getMilliseconds() //millisecond
	 }
	 if(/(y+)/.test(format)) format=format.replace(RegExp.$1,
	 (this.getFullYear()+"").substr(4 - RegExp.$1.length));
	 for(var k in o)if(new RegExp("("+ k +")").test(format))
	 format = format.replace(RegExp.$1,
	 RegExp.$1.length==1 ? o[k] :
	 ("00"+ o[k]).substr((""+ o[k]).length));
	 return format;
}

function initDateTime(_formatPattern,_month,_day){
	var formatPattern = _formatPattern?_formatPattern:'yyyy-MM-dd';
	var date = new Date();
	if(_month){
		date.setMonth(date.getMonth() + _month);
	}
	if(_day){
		date.setDate(_day);
	}
	return	date.format(formatPattern);
}

var dayNumChar = {
		  1:'一月',
		  2:'二月',
		  3:'三月',
		  4:'四月',
		  5:'五月',
		  6:'六月',
		  7:'七月',
		  8:'八月',
		  9:'九月',
		  10:'十月',
		  11:'十一月',
		  12:'十二月'
		};