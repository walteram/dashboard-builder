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
                <% for(int i=0; i < xvalues.size(); i++) { if( i != 0 ) out.print(", "); %>
                {
                    "label" : "<%= xvalues.get(i) %>" ,
                    "value" : <%= yvalues.get(i) %>
                }
                <% } %>
            ]
        }
    ];

	var tooltipShowFn_<%=chartId%> = function(e, offsetElement) {
       <% if(enableTooltips) { %>
		x = e.label;
		y = d3.format(',.<%=decimalPrecision%>f')(e.value);

		content = x + " : " + y;
		document.getElementById("tooltip<%=chartId%>").innerHTML=content;

		<% } %>
	}
	
    nv.addGraph({
      generate: function() {
            var chart = nv.models.pieChart();

             chart  .x(function(d) { return d.label })
                    .y(function(d) { return d.value })
                    .width(<%= displayer.getWidth() %>)
                    .height(<%= displayer.getHeight() %>)
                    .showLegend(<%=displayer.isShowLegend() %>)
                    .showLabels(<%=displayer.isShowLabelsXAxis() %>)
					.labelThreshold(<%=displayer.getLabelThreshold()%>/100)
                    .margin({top: <%=displayer.getMarginTop()%>, right: <%=displayer.getMarginRight()%>, bottom: <%=displayer.getMarginBottom()%>, left: <%=displayer.getMarginLeft()%>});

               d3.select('#<%= chartId %> svg')
                    .datum(chartData<%=chartId%>)
<% if(animateChart) { %> .transition().duration(1200) <% } %>
                    .call(chart);

               nv.utils.windowResize(chart.update);

            return chart;

      },
      callback: function(graph) {
       <% if( enableDrillDown && !disableDrillDown) {%>
          graph.pie.dispatch.on('elementClick', function(e) {
          form = document.getElementById('<%="form"+chartId%>');
          form.<%= NVD3ChartViewer.PARAM_NSERIE %>.value = e.label;
          submitAjaxForm(form);
          });
       <% } %>

        graph.dispatch.on('tooltipShow', tooltipShowFn_<%=chartId%>);
      }
  });
</script>
