var typeMap;
var app={
    config:{
      url:'http://192.168.90.12/mes/'
      //url:'http://192.168.90.4:8080/mes-webapp/'
      //url:'http://192.168.90.91:9090/mes-webapp/'
    },
    isLogin:function() {
        if(app.localStorage('user')){
            return true;
        }
        return false;
    },
   localStorage:function(key, value) {
   		if(arguments.length === 0) {
   			app.log.warn("没有参数");
   			return;
   		}
        if(!window || !window.localStorage) {
            mui.alert("您开启了秘密浏览或无痕浏览模式，请关闭!", "提示", "确认", null,'div');
            return;
        }
        if(arguments.length === 1 || typeof(value) === "undefined") {
            return window.localStorage.getItem(key);
        } else if(value === null || value === '') {
            window.localStorage.removeItem(key);
        } else if(typeof(value) === "object") {
            window.localStorage.setItem(key, JSON.stringify(value));
        } else {
            window.localStorage.setItem(key, value);
        }
   	},
   	validateTimeOut:function(callback){
   		var date = new Date();
   		var lastTime = app.localStorage('last-time');
   		var timeOut = app.localStorage('timeOut');
   		if(lastTime && timeOut && date.getTime()-lastTime>parseInt(timeOut)*60*1000){
   			mui.alert("登录超时,请重新登录。", "提示", ["确定"], function(result){
				app.localStorage("user","");
				window.location.href="../login/login.html";
				app.localStorage("last-time","");
   			});
   		}else{
   			app.localStorage("last-time",date.getTime());
   			callback();
   		}
   	},
    ajaxRequest:function(options){
    	app.validateTimeOut(function(){
	        var data = options.data?options.data:{};
	        data['Access-Control-Allow-Origin'] = '*';
	        data['Access-Control-Allow-Methods'] = 'GET,POST';
	        $.ajax({
	            "url":app.config.url+options.url,
	             type:options.type?options.type:"GET",
	             dateType:"jsonp",
	             data:data,
	             async: false,
	             success:function(res){
	                if(res.success){
	                    if(options.callback){
	                        options.callback(res);
	                    }
	                }else{
	                    mui.alert(res.message, "提示", "确认", null,'div');
	                }
	             },
	             error:function(e){
	                 if(options.error){
	                     options.error(res);
	                 }
	             }
	        });
    	});
    },
    readTag:function(orderType){
        if(orderType && orderType == 'readEpc'){
            demo.toRead(orderType,"readEpcData");
        }else if(orderType && orderType == 'readUser'){
            demo.toRead(orderType,"readUserData");
        }
    },
    writeTag:function(orderType,strWriteData){
         demo.toWrite(orderType,strWriteData);
    },
    loadData:function(options,callback){
    		var queryParams = {};
    		if(options && options.apiUrl){
    			var values = options.formData;
    	        for (var i in values) {
    	            if (values[i] != null && values[i] != '' && values[i].length > 0) {
    	                queryParams['queryCondition[' + i + ']'] = values[i];
    	            }
    	        }
    	        if (options.pageSize) {
    		        queryParams.offset = (options.currentPage - 1) * options.pageSize;
    		        queryParams.limit = options.pageSize;
    		    }
                app.ajaxRequest({
                    url:options.apiUrl,
                    data:queryParams,
                    callback:function(r){
                        var  searchList = r.rows;
                        options.searchSongList = searchList;
                        if(searchList.length > 0){
                            if(r.currentPage == r.totalPage){//当前页等于总页数，代表已经是最后一页了，底部显示暂无数据
                              options.scrollData.noFlag = true;
                            }
                          mui.toast(r.currentPage+"/"+r.totalPage,{ duration:'short', type:'div' });
                        }else{
                            //没有更多数据了
                             options.scrollData.noFlag = true;
                        }
                        options.currentPage++;
                        callback();
                    }
                });
    	}
     },
      entryRenderer:function (key, value) {
        if (!typeMap) {
            var entrys = app.localStorage('entrys');
            if (entrys) {
                typeMap = eval("(" + entrys + ")");
            } else {
                return value;
            }
        }
        var entryMap = typeMap[key];
        if (entryMap != null && entryMap[value] != null) {
            return entryMap[value]['zh_CN'];
        } else {
            return '';
        }
   },
   getEntrys:function (key) {
    if (!typeMap) {
        var entrys = app.localStorage('entrys');
        if (entrys) {
            typeMap = eval("(" + entrys + ")");
        } else {
            return null;
        }
    }
    if(typeMap && typeMap[key]){
        var entrys= typeMap[key];
        var arr=[];
        for(var o in entrys){
            arr.push({
                code:o,
                name:entrys[o]['zh_CN']
            });
        }
        return arr;
    }
    return null;
  },
  entryRendererMulti:function(key, value) {
        if (!typeMap) {
            var entrys =  app.localStorage('entrys');
            if (entrys) {
                typeMap = eval("(" + entrys + ")");
            } else {
                return value;
            }
        }
        var entryMap = typeMap[key];
        var entryValues = "";
        if (value != null && value != "") {
            var values = value.split(",");
            for (var i = 0; i < values.length; i++) {
                if (entryMap != null && entryMap[values[i]] != null) {
                    entryValues=entryValues!=""?entryValues+",":entryValues;
                    entryValues+=entryMap[values[i]]['zh_CN'];
                }
            }
        }
        return entryValues;
       },
    keysValue:function(arrays,value){
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
       },
       initSelect:function(selectId,defaultVal,items){
              var objSelect=document.getElementById(selectId);
              objSelect.innerHTML="<option value=''>请选择</option>";
              if(items){
                  for(var i in items){
                      var objOption = document.createElement("OPTION");
                      objOption.text= items[i].name;
                      objOption.value=items[i].code;
                      if(defaultVal && defaultVal==items[i].code){
                          objOption.selected="selected";
                      }
                      objSelect.options.add(objOption);
                  }
              }
        },
        wisexec:function(val){
            var result=[];
            var p=/\[(.*?)\]/g;
            var i=0;
            var ele;
            if(val){
                while ((ele=p.exec(val))!=null){
                   result[i]=ele[1];
                   i++;
                }
            }
            return result;
        },
        buttonAuthority:function(key){
            var visible = false;
            var buttons =JSON.parse(app.localStorage('phoneButton'));
            if(buttons &&buttons.length > 0){
                for(var i in buttons){
                    if(buttons[i].code == key){
                        visible = true;
                        break;
                    }
                }
            }
            return visible;
        }
}








