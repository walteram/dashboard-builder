<%--

    Copyright (C) 2012 JBoss Inc

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>
<%
	int decimalPrecision = 2;
    if (displayer.isAxisInteger()) decimalPrecision = 0;
%> 
<script type="text/javascript" defer="defer">
	
	chartData<%=chartId%> = [
        {
            key: "<%= displayer.getTitle() %>",
            values: [
                <%
                String initialDate = "9999-99-99";
                String endingDate = "";
				for (int i=0; i< xyDataSet.getRowCount(); i++) 
				{
						%>
						{
							"label" : "<%=xyDataSet.getValueAt(i, 0)%>",
							"startDate"  : "<%=xyDataSet.getValueAt(i, 3)%>",
							"endDate"  : "<%=xyDataSet.getValueAt(i, 4)%>",
							"size" : <%=xyDataSet.getValueAt(i, 5)%>,
							"done" : <%=xyDataSet.getValueAt(i, 6)%>,
							"progress" : <%=xyDataSet.getValueAt(i, 7)%>
						}
						<%
						if (i < xyDataSet.getRowCount()-1){
							%>, <%							
						}
						if (xyDataSet.getValueAt(i, 3).toString().compareTo(initialDate) < 0){
                            initialDate = xyDataSet.getValueAt(i, 3).toString();
                        }
                        if (xyDataSet.getValueAt(i, 4).toString().compareTo(endingDate) > 0){
                            endingDate = xyDataSet.getValueAt(i, 4).toString();
                        }
				}
				%>
                
            ]
        }
    ];
	
	d3.select("#<%=chartId%>").select('svg').remove()
	for(i = chartData<%=chartId%>[0].values.length-1; i >=0 ; i--)
	{
		var item = chartData<%=chartId%>[0].values[i];
		CreateGantt('<%=chartId%>', i, item.label, item.startDate, item.endDate, item.size, item.done, item.progress, '<%=initialDate%>', '<%=endingDate%>', '<%=LocaleManager.currentLocale()%>'
            <% if( enableDrillDown && !disableDrillDown ) {%>
            , function(value){
                form = document.getElementById('<%="form"+chartId%>');
                form.<%= NVD3ChartViewer.PARAM_NSERIE %>.value = value;
                submitAjaxForm(form);                  
            }
            <% } %>);
	}
    
    CreateGanttAxis('<%=chartId%>', '<%=initialDate%>', '<%=endingDate%>', <%=xyDataSet.getRowCount()%>, '<%=LocaleManager.currentLocale()%>');    
</script>
