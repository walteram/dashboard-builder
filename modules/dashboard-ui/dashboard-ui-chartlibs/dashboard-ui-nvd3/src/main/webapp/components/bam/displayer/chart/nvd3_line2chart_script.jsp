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
            key: "<%= rangeProperty.getName(locale) %>",
            area: <%= displayer.isShowLinesArea()%>,
            color: "<%=selectedColor%>",
            values: [
                <% for(int i=0; i < xvalues.size(); i++) { if( i != 0 ) out.print(", "); %>
                { "x" : <%= i %>, "y" : <%= yvalues.get(i) %> }
                <% } %>
            ]
        },
		{
            key: "<%= range2Property.getName(locale) %>",
            area: <%= displayer.isShowLinesArea()%>,
            color: "<%=selectedColor2%>",
            values: [
                <% for(int i=0; i < xvalues.size(); i++) { if( i != 0 ) out.print(", "); %>
                { "x" : <%= i %>, "y" : <%= y2values.get(i) %> }
                <% } %>
            ]
        }
    ];

  var chartLabels<%=chartId%> = [
  <% for(int i=0; i < xvalues.size(); i++) { if( i != 0 ) out.print(", "); %>
    "<%= xvalues.get(i) %>"
  <% } %>
  ];
  
  var tooltipShowFn_<%=chartId%> = function(e, offsetElement) {
       x = e.point.label;
       y = e.point.y;
       n = e.pointIndex;

       if( n >= 0 && n < <%=xvalues.size()%> ) {
           content = chartLabels<%=chartId%>[n] + " : " + y;
       } else {
           content = "";
       }

       document.getElementById("tooltip<%=chartId%>").innerHTML=content;
  }

  nv.addGraph({
  generate: function() {
            var chart = nv.models.lineChart();

            chart
                .x(function(d) { return d.x })
                .y(function(d) { return d.y })
                .width(<%= displayer.getWidth() %>)
                .height(<%= displayer.getHeight() %>)
                .margin({top: <%=displayer.getMarginTop()%>, right: <%=displayer.getMarginRight()%>, bottom: <%=displayer.getMarginBottom()%>, left: <%=displayer.getMarginLeft()%>})

               ;

            chart.xAxis.tickFormat(function(d) {
                    if( (typeof d === 'number' && d % 1 == 0) && d >= 0 && d < <%=xvalues.size()%> )
                        return <%= displayer.isShowLabelsXAxis() ? "chartLabels" + chartId + "[d]" : "" %>
                    else
                        return "";
                   });

            chart.yAxis
                    .tickFormat(d3.format(',.<%=decimalPrecision%>f'))
                    .axisLabel("<%= rangeProperty.getName(locale) %> / <%= range2Property.getName(locale) %>");

            chart.xAxis
                    .axisLabel("<%= domainProperty.getName(locale) %>");

            chart.xAxis.rotateLabels(<%=displayer.getLabelAngleXAxis()%>);

<% if(!enableTooltips) { %>
            chart.tooltips(false);
<% } %>
            d3.select('#<%= chartId %> svg')
                    .datum(chartData<%=chartId%>)
<% if(animateChart) { %> .transition().duration(500) <% } %>
                    .call(chart);


            nv.utils.windowResize(chart.update);

            return chart;

  },
  callback: function(graph) {
  <% if( enableDrillDown && !disableDrillDown ) {%>
    graph.lines.dispatch.on('elementClick', function(e) {
          form = document.getElementById('<%="form"+chartId%>');
          form.<%= NVD3ChartViewer.PARAM_NSERIE %>.value = chartLabels<%=chartId%>[e.pointIndex];
          submitAjaxForm(form);
          });
  <% } %>

   graph.dispatch.on('tooltipShow', tooltipShowFn_<%=chartId%>);
  }
  });  
</script>
