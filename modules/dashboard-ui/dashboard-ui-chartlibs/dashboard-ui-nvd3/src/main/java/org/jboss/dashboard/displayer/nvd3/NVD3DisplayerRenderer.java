/**
 * Copyright (C) 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.dashboard.displayer.nvd3;

import org.jboss.dashboard.LocaleManager;
import org.jboss.dashboard.annotation.Install;
import org.jboss.dashboard.annotation.config.Config;
import org.jboss.dashboard.displayer.*;
import org.jboss.dashboard.displayer.annotation.BarChart;
import org.jboss.dashboard.displayer.annotation.LineChart;
import org.jboss.dashboard.displayer.annotation.Line2Chart;
import org.jboss.dashboard.displayer.annotation.PieChart;
import org.jboss.dashboard.displayer.annotation.ProgressBarChart;
import org.jboss.dashboard.displayer.annotation.GanttChart;
import org.jboss.dashboard.displayer.annotation.TreemapChart;
import org.jboss.dashboard.displayer.annotation.RepeaterChart;
import org.jboss.dashboard.displayer.annotation.BarAndLineChart;
import org.jboss.dashboard.displayer.chart.*;
import org.jboss.dashboard.ui.UIServices;
import org.jboss.dashboard.ui.components.js.JSIncluder;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

@Install
@BarChart
@PieChart
@LineChart
@Line2Chart
@ProgressBarChart
@GanttChart
@TreemapChart
@RepeaterChart
@BarAndLineChart
public class NVD3DisplayerRenderer extends AbstractDataDisplayerRenderer {

    public static final String UID = "nvd3";

    @Inject @Config("true")
    protected boolean enabled;

    @Inject @Config("")
    protected String[] barChartTypes;

    @Inject @Config("")
    protected String barChartDefault;

    @Inject @Config("")
    protected String[] pieChartTypes;

    @Inject@Config("")
    protected String pieChartDefault;

    @Inject @Config("")
    protected String[] lineChartTypes;

    @Inject @Config("")
    protected String lineChartDefault;
	
	@Inject @Config("")
    protected String[] line2ChartTypes;

    @Inject @Config("")
    protected String line2ChartDefault;

    @Inject @Config("")
    protected String[] barAndLineChartTypes;

    @Inject @Config("")
    protected String barAndLineChartDefault;
	
	@Inject @Config("")
    protected String[] progressBarChartTypes;
	
	@Inject @Config("")
    protected String[] ganttChartTypes;
	
	@Inject @Config("")
    protected String[] repeaterChartTypes;
	
	@Inject @Config("")
    protected String[] treemapChartTypes;

    @Inject @Config("")
    protected String progressBarChartDefault;
	
	@Inject @Config("")
    protected String ganttChartDefault;
	
	@Inject @Config("")
    protected String repeaterChartDefault;
	
	@Inject @Config("")
    protected String treemapChartDefault;

    protected List<DataDisplayerFeature> featuresSupported;
    protected Map<String, List<String>> availableChartTypes;
    protected Map<String, String> defaultChartTypes;

    @Inject
    protected LocaleManager localeManager;

    @PostConstruct
    protected void init() {
        // Define the displaying features supported.
        featuresSupported = new ArrayList<DataDisplayerFeature>();
        featuresSupported.add(DataDisplayerFeature.ALIGN_CHART);
        featuresSupported.add(DataDisplayerFeature.SHOW_TITLE);
        featuresSupported.add(DataDisplayerFeature.SHOW_HIDE_LABELS);
        featuresSupported.add(DataDisplayerFeature.ROUND_TO_INTEGER);
        //featuresSupported.add(DataDisplayerFeature.SET_CHART_TYPE);
        featuresSupported.add(DataDisplayerFeature.SET_CHART_WIDTH);
        featuresSupported.add(DataDisplayerFeature.SET_CHART_HEIGHT);
        //featuresSupported.add(DataDisplayerFeature.SET_FOREGRND_COLOR);
        featuresSupported.add(DataDisplayerFeature.SET_LABELS_ANGLE);
        featuresSupported.add(DataDisplayerFeature.SET_MARGIN_BOTTOM);
        featuresSupported.add(DataDisplayerFeature.SET_MARGIN_TOP);
        featuresSupported.add(DataDisplayerFeature.SET_MARGIN_LEFT);
        featuresSupported.add(DataDisplayerFeature.SET_MARGIN_RIGHT);
        //featuresSupported.add(DataDisplayerFeature.SHOW_LINES_AREA);
        featuresSupported.add(DataDisplayerFeature.DISABLE_DRILL_DOWN);

        // Register the available chart types.
        availableChartTypes = new HashMap<String, List<String>>();
        availableChartTypes.put(BarChartDisplayerType.UID, Arrays.asList(barChartTypes));
        availableChartTypes.put(PieChartDisplayerType.UID, Arrays.asList(pieChartTypes));
        availableChartTypes.put(LineChartDisplayerType.UID, Arrays.asList(lineChartTypes));
        availableChartTypes.put(Line2ChartDisplayerType.UID, Arrays.asList(line2ChartTypes));
        availableChartTypes.put(BarAndLineChartDisplayerType.UID, Arrays.asList(barAndLineChartTypes));
        availableChartTypes.put(ProgressBarChartDisplayerType.UID, Arrays.asList(progressBarChartTypes));
        availableChartTypes.put(GanttChartDisplayerType.UID, Arrays.asList(ganttChartTypes));
        availableChartTypes.put(RepeaterChartDisplayerType.UID, Arrays.asList(repeaterChartTypes));
        availableChartTypes.put(TreemapChartDisplayerType.UID, Arrays.asList(treemapChartTypes));

        // Set the default chart type for each displayer type.
        defaultChartTypes = new HashMap<String, String>();
        defaultChartTypes.put(BarChartDisplayerType.UID, barChartDefault);
        defaultChartTypes.put(PieChartDisplayerType.UID, pieChartDefault);
        defaultChartTypes.put(LineChartDisplayerType.UID, lineChartDefault);
        defaultChartTypes.put(Line2ChartDisplayerType.UID, line2ChartDefault);
        defaultChartTypes.put(BarAndLineChartDisplayerType.UID, barAndLineChartDefault);
        defaultChartTypes.put(ProgressBarChartDisplayerType.UID, progressBarChartDefault);
        defaultChartTypes.put(GanttChartDisplayerType.UID, ganttChartDefault);
        defaultChartTypes.put(RepeaterChartDisplayerType.UID, repeaterChartDefault);
        defaultChartTypes.put(TreemapChartDisplayerType.UID, treemapChartDefault);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getUid() {
        return UID;
    }

    public String getDescription(Locale l) {
        try {
            ResourceBundle i18n = localeManager.getBundle("org.jboss.dashboard.displayer.nvd3.messages", l);
            return i18n.getString("nvd3.name");
        } catch (Exception e) {
            return "SVG Charts (NVD3)";
        }
    }

    public boolean isFeatureSupported(DataDisplayer displayer, DataDisplayerFeature feature) {
        if (displayer instanceof PieChartDisplayer && feature.equals(DataDisplayerFeature.SHOW_LEGEND)) {
            return true;
        }
		if (displayer instanceof PieChartDisplayer && feature.equals(DataDisplayerFeature.SET_LABEL_THRESHOLD)) {
            return true;
        } else
        if (((displayer instanceof LineChartDisplayer) || (displayer instanceof Line2ChartDisplayer) || (displayer instanceof BarAndLineChartDisplayer)) && feature.equals(DataDisplayerFeature.SHOW_LINES_AREA)) {
            return true;
        }
		if (((displayer instanceof LineChartDisplayer) || (displayer instanceof Line2ChartDisplayer) || (displayer instanceof BarAndLineChartDisplayer)) && feature.equals(DataDisplayerFeature.SET_FOREGRND_COLOR)) {
            return true;
        }
		if ((displayer instanceof Line2ChartDisplayer || displayer instanceof BarAndLineChartDisplayer) && feature.equals(DataDisplayerFeature.SET_RANGE2)) {
            return true;
        }
		if ((displayer instanceof Line2ChartDisplayer || displayer instanceof BarAndLineChartDisplayer) && feature.equals(DataDisplayerFeature.SET_FOREGRND_COLOR2)) {
            return true;
        }
		if (displayer instanceof ProgressBarChartDisplayer && (feature.equals(DataDisplayerFeature.SET_STARTDATE_PROP) || feature.equals(DataDisplayerFeature.SET_ENDDATE_PROP) || feature.equals(DataDisplayerFeature.SET_SIZE_PROP) || feature.equals(DataDisplayerFeature.SET_DONE_PROP)  || feature.equals(DataDisplayerFeature.SET_PROGRESS_PROP) )) {
            return true;
        }
		if (displayer instanceof GanttChartDisplayer && (feature.equals(DataDisplayerFeature.SET_STARTDATE_PROP) || feature.equals(DataDisplayerFeature.SET_ENDDATE_PROP) || feature.equals(DataDisplayerFeature.SET_SIZE_PROP) || feature.equals(DataDisplayerFeature.SET_DONE_PROP)  || feature.equals(DataDisplayerFeature.SET_PROGRESS_PROP) )) {
            return true;
        }
		if (displayer instanceof BarChartDisplayer && feature.equals(DataDisplayerFeature.FIXED_COLOR)) {
            return true;
        }
		else {
            return featuresSupported.contains(feature);
        }
    }

    public List<String> getAvailableChartTypes(DataDisplayer displayer) {
        DataDisplayerType displayerType = displayer.getDataDisplayerType();
        return availableChartTypes.get(displayerType.getUid());
    }

    public String getDefaultChartType(DataDisplayer displayer) {
        DataDisplayerType displayerType = displayer.getDataDisplayerType();
        return defaultChartTypes.get(displayerType.getUid());
    }

    public String getChartTypeDescription(String chartType, Locale locale) {
        try {
            ResourceBundle i18n = localeManager.getBundle("org.jboss.dashboard.displayer.nvd3.messages", locale);
            return i18n.getString("nvd3.type." + chartType);
        } catch (Exception e) {
            return chartType;
        }
    }

    public void setDefaultSettings(DataDisplayer displayer) {
        if (displayer instanceof AbstractChartDisplayer) {
            AbstractChartDisplayer chartDisplayer = (AbstractChartDisplayer) displayer;
            chartDisplayer.setMarginLeft(80);
            chartDisplayer.setMarginBottom(100);
            chartDisplayer.setUseProgressColumns(false);
        }
        if (displayer instanceof AbstractXAxisDisplayer) {
            AbstractXAxisDisplayer xAxisDisplayer = (AbstractXAxisDisplayer) displayer;
            xAxisDisplayer.setLabelAngleXAxis(-45);
        }
        if (displayer instanceof LineChartDisplayer) {
            LineChartDisplayer lineChartDisplayer = (LineChartDisplayer) displayer;
            lineChartDisplayer.setShowLinesArea(true);
        }
        if (displayer instanceof Line2ChartDisplayer) {
            Line2ChartDisplayer line2ChartDisplayer = (Line2ChartDisplayer) displayer;
            line2ChartDisplayer.setShowLinesArea(true);
        }
        if (displayer instanceof BarAndLineChartDisplayer) {
            BarAndLineChartDisplayer barAndLineChartDisplayer = (BarAndLineChartDisplayer) displayer;
            barAndLineChartDisplayer.setShowLinesArea(true);
        }
        if (displayer instanceof ProgressBarChartDisplayer) {
            ProgressBarChartDisplayer progressBarChartDisplayer = (ProgressBarChartDisplayer) displayer;
            progressBarChartDisplayer.setUseProgressColumns(true);
        }
		if (displayer instanceof GanttChartDisplayer) {
            GanttChartDisplayer ganttChartDisplayer = (GanttChartDisplayer) displayer;
            ganttChartDisplayer.setUseProgressColumns(true);
        }
    }
}

