var baseUrl = "/internal"
var updateUrl = "/internal/pages/update"
var title = ''
var values = []
var dataMap = []
var colspans = []
var lastId = ['']
var pageSize = 255
var readonlySet = ['sn', 'deleted', 'createtime', 'updatetime']
function setCookie(name,value)
{
	var Days = 30;
	var exp = new Date();
	exp.setTime(exp.getTime() + Days*24*60*60*1000);
	document.cookie = name + "="+ escape (value) + ";expires=" + exp.toGMTString();
}

//读取cookies
function getCookie(name)
{
	var arr,reg=new RegExp("(^| )"+name+"=([^;]*)(;|$)");
	if(arr=document.cookie.match(reg))
		return unescape(arr[2]);
	else
		return null;
}

//删除cookies
function delCookie(name)
{
	var exp = new Date();
	exp.setTime(exp.getTime() - 1);
	var cval=getCookie(name);
	if(cval!=null) {
		document.cookie= name + "="+cval+";expires="+exp.toGMTString();
	}
}
function clean(txt) {
	if(txt === undefined) return ""
	return ("" + txt).replace(/</g, "< ").replace(/>/g," >")
}
//下一页
function loadmore() {
	console.log('load more')
	var startId = lastId.pop()
	if (startId === undefined) {
		startId = ''
	}
	lastId.push(startId)
	jQuery.ajax({
		url: baseUrl + "/"+target+"/page?pageSize="+pageSize+"&lastId="+startId+"&r=" + Math.random(),
		success: function( result ) {
			$("#load-more").show()
			$("#load-less").show()
			var nextId = ''
			console.log("[joinnearby] "+target+" data success " + result)

			dataMap = result.data
			var table = '<table class="max-table">\n'
			for(var i=0;i<dataMap.length;i++) {
				var obj = dataMap[i]
				table = table + '<tr data-itemid="'+i+'">\n'
				for(var j=0;j<values.length;j++) {
					var name = values[j].name
					var id = name + '_' + i + 'th'

					if(name === 'sn') {
						table = table + '<td colspan="0" style="display:none" id="'+id+'"></td>\n'
					} else if(values[j].visible) {
						table = table + '<td colspan="'+colspans[j]+'" class="data-item" id="'+id+'">' + clean(obj[name]) + '</td>\n'
					}

					nextId = obj['sn']
				}
				table = table + '</tr>\n'
			}
			if(dataMap.length < 1) {
				table = table + '<tr>Nothing to be shown</tr>'
			}
			if(dataMap.length < pageSize) {
				$("#load-more").hide()
			}
			if(lastId.length < 2) {
				console.log('we are in the first page right now!')
				$("#load-less").hide()
			}
			table = table + '</table>\n'
			$("#pages-data").html(table)

			lastId.push(nextId)
			console.log("lastId = " + lastId)
			// 点击一条弹出一筐
			$("td.data-item").click(function() {
				var tr = $(this).parent()
				var itemid = tr.data('itemid')
				console.log(itemid)
				popItem(itemid)
			});
		},
		error: function( xhr, result, obj ) {
			console.log("[joinnearby] "+target+" head error " + result)
		}
	})
	console.log('more loaded')
}
//上一页
function loadless() {
	console.log('load less')
	console.log('remove no use lastId: ' + lastId.pop())
	console.log('remove no use lastId: ' + lastId.pop())
	loadmore()
	console.log('less loaded')
}

//搜索关键词
function searchitem(text) {
	if(text.length < 1) {
		window.location.reload();
	}
	jQuery.ajax({
		url: baseUrl + "/"+target+"/page?pageSize=128&&search="+text+"&r=" + Math.random(),
		success: function( result ) {
			$("#load-more").hide()
			$("#load-less").hide()
			$("#search").val('')
			console.log("[joinnearby] "+target+" data success ")

			dataMap = result.data
			var table = '<table class="max-table">\n'
			for(var i=0;i<dataMap.length;i++) {
				var obj = dataMap[i]
				table = table + '<tr data-itemid="'+i+'">\n'
				for(var j=0;j<values.length;j++) {
					var name = values[j].name
					var id = name + '_' + i + 'th'
					if(values[j].visible) {
						table = table + '<td colspan="'+colspans[j]+'" class="data-item" id="'+id+'">' + clean(obj[name]) + '</td>\n'
					}
				}
				table = table + '</tr>\n'
			}
			if(dataMap.length < 1) {
				table = table + '<tr>TMD老子搜不到</tr>'
			}

			table = table + '</table>\n'
			$("#pages-data").html(table)
			$("#total").text('总共 ' + result.total + ' 条')
			console.log("lastId = " + lastId)
			$("td.data-item").click(function() {
				var tr = $(this).parent()
				var itemid = tr.data('itemid')
				console.log(itemid)
				popItem(itemid)
			});
		},
		error: function( xhr, result, obj ) {
			console.log("[joinnearby] "+target+" head error " + result)
		}
	})
}
//提交新增
function submitcreate() {
	var createjson = {}
	var items = $(".create-item")
	var allValue = ''
	for(var i=0;i<items.length;i++)
	{
		var item = items[i]
		var name = $(item).attr('name')
		var value = $(item).val()
		if(value && value.length > 0) {
			createjson[name] = value
			allValue = allValue + value
		}
	}
	if(allValue.length > 1) {
		jQuery.post({
			url: baseUrl + "/" + target + "/insert",
			dataType: 'json',
			contentType:'application/json',
			data: JSON.stringify(createjson),
			success: function (result) {
				$("#the-modal").modal('hide')
				window.location.reload();
			},
			error: function (xhr, result, obj) {
				console.log("[joinnearby] " + target + " insert error " + result)
			}
		})
	}

}
//创建记录
function createitem() {
	if(values.length < 1) {
		console.log('头部信息为空，无法新增')
	}
	$("#the-modal").modal("show")
	$(".modal-title").html("<button type='button' class='btn btn-outline-primary' onclick='submitcreate()'>新增 <span class='glyphicon glyphicon-send'></span></button>")

	txt = '<table class="min-table">'
	for(var i=0;i<values.length;i++) {
		var obj = values[i]
		var name = obj.name
		var value = obj.value
		if(readonlySet.indexOf(name) > -1) {
			continue
		}
		txt = txt + '<tr><td><label class="form-label">' + value + '</label></td><td> <input type="text" class="form-control create-item" name="'+name+'" value=""/><td></tr>'
	}
	txt = txt + '</table>'
	$(".modal-body").html(txt)
}
//彻底删除记录
function removeitem(itemid) {
	console.log('removeitem: ' + itemid)
	var obj = dataMap[itemid]
	var sn = obj['sn']
	jQuery.post({
		url: baseUrl + "/" + target + "/delete/" + sn + '?remove=true',
		dataType: 'json',
		contentType:'application/json',
		success: function (result) {
			console.log("[joinnearby] " + target + " delete ok " + result)
			$("#sn_" + itemid + "th").parent().remove()
			$("#the-modal").modal('hide')
		},
		error: function (xhr, result, obj) {
			console.log("[joinnearby] " + target + " delete error " + result)
		}
	})
}
function updatetable(iname, itemid, value) {
	var th = iname + '_' + itemid + 'th'
	$('#' + th).text(value)
}
function updatedata(iname, itemid, value) {
	var obj = dataMap[itemid]
	obj[iname] = value
	console.log('updated data: ' + obj[iname])
}

//确认
function submititem(iname, itemid) {
	var id = iname + '_' + itemid
	console.log('submititem: ' + id)
	var obj = dataMap[itemid]
	var sn = obj['sn']
	var oldV = obj[iname]
	var input = $('#' + id)
	var value = input.val()
	console.log('ajax sending value to api: ' + value)
	jQuery.post({
		url: updateUrl + "/" + target,
		dataType: 'json',
		contentType:'application/json',
		data: JSON.stringify({
			title: iname,
			sn: sn,
			newVal: value,
			oldVal: oldV
		}),
		success: function (result) {
			console.log("[joinnearby] " + target + " update ok " + result)
			if(iname === 'deleted') {
				$("#sn_" + itemid + "th").parent().remove()
				$("#the-modal").modal('hide')
			}
		},
		error: function (xhr, result, obj) {
			console.log("[joinnearby] " + target + " update error " + result)
		}
	})
	//update input value
	var td = input.parent()
	$(td).text(value)
	//update btn color
	var obj = $(td.parent()).siblings()[0]
	$(obj).parent().css('background', '')
	var span = $(obj).find('span')[0];
	if($(span).hasClass('glyphicon-log-out')) {
		$(span).removeClass('glyphicon-log-out')
		$(span).addClass('glyphicon-edit')
		$(span).text('改')
	}
	//update page
	updatetable(iname, itemid, value)
	updatedata(iname, itemid, value)
}
//点击修改
function updateitem(obj, iname, itemid) {
	var id = iname + '_' + itemid
	console.log('updateitem: ' + id)

	var td = $(obj).parent()
	var input = $(td).siblings()[1]
	var span = $(obj).find('span')[0];
	if($(span).hasClass('glyphicon-edit')) {
		var text = $(input).text()
		$(input).html('<div class="input-group"><input type="text" class="form-control" id="'+id+'" name="update" value=""/><button type="button" class="btn btn-outline-danger btn-sm" onclick="submititem(\'' + iname + '\',' + itemid + ')"><span class="glyphicon glyphicon-ok-circle"></span>存</button></div>')
		$(span).removeClass('glyphicon-edit')
		$(span).addClass('glyphicon-log-out')
		$(span).text('x')
		$('#'+id).val(text)
	} else if($(span).hasClass('glyphicon-log-out')) {
		var index = parseInt(itemid)
		var item = dataMap[index]
		var text = clean(item[iname])
		$(input).html('<td>' + text + '</td>')
		$(span).removeClass('glyphicon-log-out')
		$(span).addClass('glyphicon-edit')
		$(span).text('改')
	}
	// $('#password' + id).bind('keyup', function(event) {
	// 	if (event.keyCode == "13") {
	// 		submititem(iname, itemid)
	// 	}
	// });
}
//加载第一页数据
function first() {
	console.log('ajax data for ' + target)
	jQuery.ajax({
		url: baseUrl + "/"+target+"/page?pageSize="+pageSize+"&r=" + Math.random(),
		success: function( result ) {
			$("#load-more").show()
			$("#load-less").hide()
			let nextId = ''
			console.log("[joinnearby] "+target+" data success ")

			dataMap = result.data

			for(var i=0;i<Math.min(5, dataMap.length);i++) {
				let obj = dataMap[i]
				for (var j = 0; j < values.length; j++) {
					let name = values[j].name
					if(name === 'sn') {
						colspans[j] = 0
						continue
					}
					if(i === 0) {
						if(obj[name]) {
							colspans[j] = Math.max(values[j].name.length,(obj[name]+"").length)
						} else {
							colspans[j] = Math.min(values[j].name.length,5)
						}
					} else {
						if(obj[name] && colspans[j] < (obj[name]+"").length) {
							colspans[j] = (obj[name]+"").length
						}
					}
				}
			}

			var header = '<table class="max-table"><tr>\n'
			for(var i=0;i<values.length;i++) {
				if(values[i].visible) {
					header = header + '<td colspan="'+colspans[i]+'">' + values[i].value + '</td>\n'
				}
			}
			header = header + '</tr></table>\n'
			$("#pages-head").html(header)
			$("#pages-head").attr("head", values.length)
			$("#pages-head").attr("name", title)

			var table = '<table class="max-table">\n'
			for(var i=0;i<dataMap.length;i++) {
				let obj = dataMap[i]
				table = table + '<tr data-itemid="'+i+'">\n'
				for(var j=0;j<values.length;j++) {
					let name = values[j].name
					let id = name + '_' + i + 'th'

					if(name === 'sn') {
						table = table + '<td colspan="0" style="display:none" id="'+id+'"></td>\n'
					} else if(values[j].visible) {
						table = table + '<td colspan="'+colspans[j]+'" class="data-item" id="'+id+'">' + clean(obj[name]) + '</td>\n'
					}
					nextId = obj['sn']
				}
				table = table + '</tr>\n'
			}
			if(dataMap.length < 1) {
				table = table + '<tr>Nothing to be shown</tr>'
			}
			if(dataMap.length < pageSize) {
				$("#load-more").hide()
				$("#load-less").hide()
			}
			table = table + '</table>\n'
			$("#pages-data").html(table)
			$("#total").text('总共 ' + result.total + ' 条')
			lastId.push(nextId)
			console.log("lastId = " + lastId)

			$("td.data-item").click(function() {
				let tr = $(this).parent()
				let itemid = tr.data('itemid')
				popItem(itemid)
			});
		},
		error: function( xhr, result, obj ) {
			console.log("[joinnearby] "+target+" head error " + result)
		}
	})
}

function popItem(itemid) {
	console.log(itemid)
	$("#modal-title").html('<button type="button" class="btn btn-danger" onclick="removeitem('+itemid +')"> 真删</button>')

	var index = parseInt(itemid)
	if(itemid >= dataMap.length) {
		loadmore();
		popItem(0)
		return
	}
	var item = dataMap[index]

	var itemhtml = '<table class="table">'
	for(var k=0;k<values.length;k++) {
		var iname = values[k].name
		var ivalue = values[k].value
		var ireadonly = values[k].readonly
		var itype = values[k].type
		var idata = item[iname]
		if(ireadonly) {
			if(itype === "img") {
				if(idata === undefined || idata === 'null' || idata === '') {
					idata = item['host'] + '/' + item['name']
				}
				itemhtml = itemhtml + '<tr><td></td><td>' + ivalue + '</td><td><img src="' + idata + '" /></td></tr>'
			} else {
				itemhtml = itemhtml + '<tr><td></td><td>' + ivalue + '</td><td>' + clean(idata) + '</td></tr>'
			}
		} else {
			if(itype === "case") {
				var ichoise = values[k].choise
				var options = ''
				for(var i=0;i<ichoise.length;i++) {
					var kv = ichoise[i]
					if(idata === kv.key) {
						options = options + '<option value="'+kv.key+'" selected>'+kv.value+'</option>'
					} else {
						options = options + '<option value="'+kv.key+'">'+kv.value+'</option>'
					}
				}
				var select = '<select class="form-select" id="' + iname + '_' + itemid + '" onchange="submititem(\''+iname+'\', \''+itemid+'\')">' + options + '</select>'
				itemhtml = itemhtml + '<tr><td></td><td>' + ivalue + '</td><td>' + select + '</td></tr>'
			} else if(itype === "img") {
				if(idata === undefined || idata === 'null' || idata === '') {
					idata = item['host'] + '/' + item['name']
				}
				itemhtml = itemhtml + '<tr><td><button type="button" class="btn btn-outline-dark btn-sm" onclick="updateitem(this, \'' + iname + '\',' + itemid + ')"> <span class="glyphicon glyphicon-edit">改</span></button></td><td>' + ivalue + '</td><td><img style="width: 300px;" src="' + idata + '" /></td></tr>'
			} else {
				itemhtml = itemhtml + '<tr><td><button type="button" class="btn btn-outline-dark btn-sm" onclick="updateitem(this, \'' + iname + '\',' + itemid + ')"> <span class="glyphicon glyphicon-edit">改</span></button> </td><td>' + ivalue + '</td><td>' + clean(idata) + '</td></tr>'
			}
		}
	}
	var lastone = itemid-1
	var nextone = itemid+1
	itemhtml = itemhtml + '<tr><td></td><td><button type="button" class="btn btn-outline-dark" onclick="popItem('+lastone+')">上一条</button></td><td><button type="button" class="btn btn-outline-dark" onclick="popItem('+nextone+')">下一条</button></td></tr>'
	itemhtml = itemhtml+'</table>'
	$("#modal-body").html(itemhtml)
	$("#the-modal").modal('show')
}

//加载头部信息
function head() {
	console.log('ajax head for ' + target)
	jQuery.ajax({
	    url: baseUrl + "/pages/head/"+target+"?r=" + Math.random(),
	    success: function( result ) {
	        console.log("[joinnearby] "+target+" head success")
			var state = result.state
			if(state === 403) {
				$("#pages-head").empty()
				$("#pages-data").empty()
				$("#total").empty()
				window.location.href = "/server/boss"
				alert("去签名登录吧")
			}
			var data = result.data
			title = data.name
			values = data.values
			first()
	    },
	    error: function( xhr, result, obj ) {
	      console.log("[joinnearby] "+target+" head error " + result)
	    }
	})
}

$("#pages-head").empty()
$("#pages-data").empty()
$("#total").empty()
$(document).ready(function(){
	$('.'+target).addClass('active')
	jQuery.ajax({
		url: baseUrl + "/pages/heads?r=" + Math.random(),
		success: function( result ) {
			console.log("[joinnearby] heads success")
			var data = result.data
			if(data.length > 0) {
				$(".navbar-nav").empty()
			}
			for(var i=0;i<data.length;i++) {
				var css = ''
				var kv = data[i]
				if(kv.key === target) {
					css = 'active'
				}
				var li = '<li class="nav-item '+kv.key+'"><a class="nav-link '+css+'" href="#'+kv.key+'" onclick="page(\''+kv.key+'\')">'+kv.value+'</a></li>'
				$(".navbar-nav").append(li)
			}
		},
		error: function( xhr, result, obj ) {
			console.log("[joinnearby] heads error " + result)
		}
	})

	var token = getCookie("token")

	$.ajaxSetup({
		headers:{'token':token}
	});
	$("#log-out").show()
	$("#log-out").click(function () {
		delCookie('token')
		$("#log-out").hide()
		$("#pages-head").empty()
		$("#pages-data").empty()
		$("#total").empty()
		window.location.href = "/server/boss"
		alert("去签名登录吧")
	})
	head()

	$(".close").click(function () {
		$("#the-modal").modal('hide')
	})
});
