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
<%@ page import="org.jboss.dashboard.LocaleManager" %>
<%@ page import="org.jboss.dashboard.ui.components.chart.AbstractChartDisplayerEditor" %>
<%@ page import="org.jboss.dashboard.displayer.chart.AbstractChartDisplayer" %>
<%@ page import="org.jboss.dashboard.provider.DataProperty" %>
<%@ page import="java.util.Locale" %>
<%@ page import="org.jboss.dashboard.domain.RangeConfiguration" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="org.jboss.dashboard.DataDisplayerServices" %>
<%@ page import="java.util.List" %>
<%@taglib uri="factory.tld" prefix="factory"%>
<%@taglib uri="mvc_taglib.tld" prefix="mvc"%>
<%@ taglib uri="bui_taglib.tld" prefix="panel"%>
<%@ taglib prefix="static" uri="static-resources.tld" %>
<%@taglib uri="http://dashboard.jboss.org/taglibs/i18n-1.0" prefix="i18n"%>
<i18n:bundle id="bundle" baseName="org.jboss.dashboard.displayer.messages" locale="<%=LocaleManager.currentLocale()%>"/>
<%
    Locale locale = LocaleManager.currentLocale();
    AbstractChartDisplayerEditor editor = (AbstractChartDisplayerEditor) request.getAttribute("editor");
    AbstractChartDisplayer displayer = (AbstractChartDisplayer) editor.getDataDisplayer();
    DataProperty range2Property = displayer.getRange2Property();
%>
    <td align="left">
        <select name="idRange2Details" title="<%= range2Property.getName(locale) %>" id="<factory:encode name="idRange2Details"/>" class="skn-input"
                style="width:95px; height:18px; text-align:left; overflow:hidden; vertical-align:middle"
                onChange="return bam_kpiedit_submitProperties(this);"
                >
        <%
            List<DataProperty> range2Properties = Arrays.asList(displayer.getRangePropertiesAvailable().clone());
            DataDisplayerServices.lookup().getDataProviderManager().sortDataPropertiesByName(range2Properties, true);

            for (DataProperty dataProperty : range2Properties) {
                String selected = "";
                if (dataProperty.getPropertyId().equals(range2Property.getPropertyId())) selected = "selected";
        %>
                <option title="<%= dataProperty.getName(locale) %>" value="<%= dataProperty.getPropertyId() %>" <%= selected %>>
                    <%= dataProperty.getName(locale) %>
                </option>
        <%
            }
        %>
        </select><a style="border:0" href="#" onclick="window.<factory:encode name="editRange2"/>(); return false;"><img
                   src="<static:image relativePath="general/16x16/ico-edit.png"/>" border="0"
                   title="<i18n:message key='<%= AbstractChartDisplayerEditor.I18N_PREFFIX + "editar_range"%>'>!!Editar range</i18n:message>"
                   style="vertical-align:middle">
        </a>
    </td>
    <td align="left">
        <div id="<factory:encode name="idRange2"/>" align="center" style="width:500px;height:400px;overflow:-moz-scrollbars-horizontal; overflow-x:hidden;overflow-y:auto; display:none;position:absolute;vertical-align:middle;z-index:11;">
            <table width="100%" align="left" cellpadding="4"  bgcolor="#FFFFFF" cellspacing="1" border="0" class="skn-table_border">
                <tr class="skn-table_header">
                    <td colspan="5">
                        <i18n:message key='<%= AbstractChartDisplayerEditor.I18N_PREFFIX + "editar_range2"%>'>!!Editar range</i18n:message>
                    </td>
                </tr>
                <% request.setAttribute("range2Config", new RangeConfiguration(displayer.getRange2Property(), displayer.getRange2ScalarFunction(), displayer.getUnitI18nMap())); %>
                <mvc:include page="range2_details.jsp" flush="true" />
                <% request.removeAttribute("range2Config"); %>
            </table>
        </div>
    </td>
    <script defer="true">
        window.<factory:encode name="editRange2"/> = function() {
            var element = document.getElementById('<factory:encode name="idRange2"/>');
            if (element.style.display == "none") element.style.display = "block";
            else element.style.display = "none";
        }
    </script>