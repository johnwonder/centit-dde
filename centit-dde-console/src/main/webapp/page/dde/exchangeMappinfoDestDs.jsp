<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/page/common/taglibs.jsp"%>


<div id="div_exchange_destds_form" class="pageContent">
	<s:form action="#" class="pageForm required-validate" onsubmit="return validateCallback(this, dialogAjaxDone);">
		<input id="hid_export_field_form_columnno" type="hidden" name="columnNo" value="${param.columnNo}"/>
		<div class="pageFormContent" layoutH="58">		
			<div class="unit">
				<label>数据库连接：</label> 
				<select id="sel_destds"  name="destDatabaseName">
				    <c:forEach var="database" items="${databaseInfos}">
      				  <option db_type = "${database.databaseType}" value="${database.databaseName}">${database.databaseName}</option>
      			   </c:forEach>
				</select>

			</div>

			<div class="unit">
				<label>表名：</label> <input id="txt_destTablename" name="destTableName" type="text" size="40" />

			</div>

            <div class="unit">
                <label>查询语句：</label>
                <textarea id="txt_dest_querySql" type="text" cols="60" rows="10" ></textarea>

            </div>

		</div>
			
		<div class="formBar">
			<ul>
				<li><div class="buttonActive">
						<div class="buttonContent">
							<button id="btn_dest_ds" type="button">保存</button>
						</div>
					</div></li>
				<li>
					<div class="button">
						<div class="buttonContent">
							<button id="btn_dest_ds_close" type="button" class="close">取消</button>
						</div>
					</div>
				</li>
			</ul>
		</div>
	</s:form>
</div>

<script src="${pageContext.request.contextPath }/scripts/module/dde/exchange/exchange.js"></script>
<script src="${pageContext.request.contextPath }/scripts/module/dde/public.js"></script>
<script>
	$(function(){
		var port = new ExportSql();
		port.pubfuns.defDestDsInit();
		port.bind("bindDestDs");
		port.bind("bindToUpperCase");
	});
</script>

