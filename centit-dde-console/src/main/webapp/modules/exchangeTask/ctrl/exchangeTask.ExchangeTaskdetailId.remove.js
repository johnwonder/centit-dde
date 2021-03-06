define(function(require) {
	require('plugins/extend');

	var Config = require('config');
	var Core = require('core/core');
	var Page = require('core/page');

	var ExchangeTaskExchangeTaskdetailIdRemove = Page.extend(function() {
		var _self = this;
		// @override
    this.removeListItem = function (table, row) {
      var index = table.datagrid('getRowIndex', row);
      table.datagrid('deleteRow', index);
    }
		this.submit = function(table, data,panel) {
      this.removeListItem(table, data);
      /*var mapinfoId = data.mapinfoId;
            data = _self.parent.data;
            var taskId = data.taskId;
            Core.ajax(Config.ContextPath+'service/exchangetaskdetail/delete?taskId='+taskId+"&MapinfoId="+mapinfoId, {
                type: 'json',
                method: 'post',
                data: {
                    _method: 'delete'
                }
            }).then(function() {
                Core.ajax(Config.ContextPath+'service/exchangetask/edit/'+taskId, {
                    method: 'get',
                    data: {
                        _method: 'get'
                    }
                }).then(function(data2) {
                    /!*$("#dg4 tbody").html("");
                    var tab1table = panel.find('table.tab1');
                    tab1table.cdatagrid({
                        controller:_self,
                        editable: true,
                        data:data2.exportSqlList
                    });*!/
                    table.datagrid('loadData',data2.exchangeMapinfoList);
                });
            });*/
		}
	});
	
	return ExchangeTaskExchangeTaskdetailIdRemove;
});
