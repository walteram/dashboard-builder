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
package org.jboss.dashboard.displayer.chart;

import org.apache.commons.lang3.StringUtils;
import org.jboss.dashboard.DataDisplayerServices;
import org.jboss.dashboard.LocaleManager;
import org.jboss.dashboard.dataset.DataSet;
import org.jboss.dashboard.displayer.AbstractDataDisplayer;
import org.jboss.dashboard.displayer.DataDisplayer;
import org.jboss.dashboard.displayer.exception.DataDisplayerInvalidConfiguration;
import org.jboss.dashboard.domain.DomainConfiguration;
import org.jboss.dashboard.domain.RangeConfiguration;
import org.jboss.dashboard.function.CountFunction;
import org.jboss.dashboard.function.ScalarFunction;
import org.jboss.dashboard.profiler.CodeBlockTrace;
import org.jboss.dashboard.profiler.CodeBlockType;
import org.jboss.dashboard.profiler.CoreCodeBlockTypes;
import org.jboss.dashboard.provider.DataProperty;
import org.jboss.dashboard.provider.DataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Base class for the implementation of chart-like data displayers.
 */
public abstract class AbstractChartDisplayer extends AbstractDataDisplayer {

    /** Logger */
    private transient static Logger log = LoggerFactory.getLogger(AbstractChartDisplayer.class);

    /**
     * The default unit value pattern.
     */
    public static final String UNIT_VALUE_TAG = "{value}";

    protected transient DataProperty domainProperty;
    protected transient DataProperty rangeProperty;
    protected transient DataProperty range2Property;
    protected transient ScalarFunction rangeScalarFunction;
    protected transient ScalarFunction range2ScalarFunction;
    protected transient Map<Locale, String> unitI18nMap;
    protected transient DomainConfiguration domainConfig;
    protected transient RangeConfiguration rangeConfig;
    protected transient RangeConfiguration range2Config;

	protected transient DataProperty startDateProperty;
	protected transient DataProperty endDateProperty;
	protected transient DataProperty sizeProperty;
	protected transient DataProperty doneProperty;
	protected transient DataProperty progressProperty;
	protected transient DomainConfiguration startDateConfig;
	protected transient DomainConfiguration endDateConfig;
	protected transient DomainConfiguration sizeConfig;
	protected transient DomainConfiguration doneConfig;	
	protected transient DomainConfiguration progressConfig;	
	
    public static final int INTERVALS_SORT_CRITERIA_LABEL = 0;
    public static final int INTERVALS_SORT_CRITERIA_VALUE = 1;
    public static final int INTERVALS_SORT_ORDER_NONE = 0;
    public static final int INTERVALS_SORT_ORDER_ASC = 1;
    public static final int INTERVALS_SORT_ORDER_DESC = -1;

    protected String type;
    protected String color;
    protected String color2;
    protected String backgroundColor;
    protected int width;
    protected int height;
    protected boolean disableDrillDown;
    protected boolean useProgressColumns;
    protected boolean showLegend;
    protected boolean axisInteger;
    protected boolean fixedColor;
    protected String legendAnchor;
    protected boolean showTitle;
    protected String title;
    protected String graphicAlign;
    protected int intervalsSortCriteria;
    protected int intervalsSortOrder;
    protected int marginLeft;
    protected int marginRight;
    protected int marginBottom;
    protected int marginTop;
    protected int labelThreshold;

    /** The flag indicating if the X-aAxis labels should be displayed. */
    protected boolean showLabelsXAxis;

    // Constructor of the class

    public AbstractChartDisplayer() {
        startDateProperty = null;
		endDateProperty = null;
		sizeProperty = null;
		doneProperty = null;
		progressProperty = null;
		startDateConfig = null;
		endDateConfig = null;
		sizeConfig = null;
		doneConfig = null;
		progressConfig = null;
		
		
		domainProperty = null;
        rangeProperty = null;
        range2Property = null;
        domainConfig = null;
        rangeConfig = null;
        range2Config = null;
        rangeScalarFunction = null;
        range2ScalarFunction = null;
        unitI18nMap = new HashMap<Locale, String>();
        color = "#FFFFFF";
        color2 = "#FFFFFF";
        backgroundColor = "#FFFFFF";
        width = 600;
        height = 300;
        disableDrillDown = false;
        useProgressColumns = false;
        showLegend = false;
        axisInteger = false;
        fixedColor = false;
        legendAnchor = "south";
        showTitle = false;
        title = null;
        graphicAlign = "center";
        intervalsSortCriteria = INTERVALS_SORT_CRITERIA_LABEL;
        intervalsSortOrder = INTERVALS_SORT_ORDER_NONE;
        marginLeft=30;
        marginRight=30;
        marginTop=30;
        marginBottom=30;
        labelThreshold=0;
    }

    public void setDataProvider(DataProvider dp) throws DataDisplayerInvalidConfiguration {

        // If the provider changes then reset the current configuration.
        if (dataProvider != null && !dataProvider.equals(dp)) {
            setDomainProperty(null);
            setRangeProperty(null);
            setRange2Property(null);
			
			setStartDateProperty(null);
			setEndDateProperty(null);
			setSizeProperty(null);
			setDoneProperty(null);
			setProgressProperty(null);
        }

        // If data provider definition does not match with displayer configuration, do not set the provider
        validate(dp);
        dataProvider = dp;
    }

    /**
     * Get the list of properties valid as domain.
     */
    public DataProperty[] getDomainPropertiesAvailable() {
        List<DataProperty> dpList = new ArrayList<DataProperty>();
        try {
            DataProperty[] props = dataProvider.getDataSet().getProperties();
            dpList.addAll(Arrays.asList(props));
        } catch (Exception e) {
            log.error("Can not retrieve dataset properties.", e);
        }
        // Build the data property array
        return dpList.toArray(new DataProperty[dpList.size()]);
    }

    /**
     * Get the list of properties valid as range.
     */
    public DataProperty[] getRangePropertiesAvailable() {
        List<DataProperty> dpList = new ArrayList<DataProperty>();
        try {
            DataProperty[] props = dataProvider.getDataSet().getProperties();
            dpList.addAll(Arrays.asList(props));
        } catch (Exception e) {
            log.error("Can not retrieve dataset properties.", e);
        }
        // Build the data property array
        return dpList.toArray(new DataProperty[dpList.size()]);
    }

    public boolean hasDataSetChanged(DataProperty property) {
        try {
            DataSet ds1 = dataProvider.getDataSet();
            DataSet ds2 = property.getDataSet();
            return (ds1 != ds2 || ds1.getRowCount() != ds2.getRowCount());
        } catch (Exception e) {
            log.error("Error getting data set.", e);
        }
        return false;
    }

    /**
     * Check if data provider definition (all properties) match with the serialized in the current displayer.
     * @param provider The data provider.
     * @throws org.jboss.dashboard.displayer.exception.DataDisplayerInvalidConfiguration Current displayer configuration is invalid.
     */
    @Override
    public void validate(DataProvider provider) throws DataDisplayerInvalidConfiguration {
        if (provider != null) {
            boolean hasDomainPropChanged = false;
            boolean hasRangePropChanged = false;

            boolean hasRange2PropChanged = false;
            boolean hasStartDatePropChanged = false;
            boolean hasEndDatePropChanged = false;
            boolean hasSizePropChanged = false;
            boolean hasDonePropChanged = false;
            boolean hasProgressPropChanged = false;
            try {
                String domainPropertyId = (domainProperty != null) ? domainProperty.getPropertyId() : (domainConfig != null) ? domainConfig.getPropertyId() : null;
                hasDomainPropChanged = hasProviderPropertiesChanged(domainPropertyId, provider);
                String rangePropertyId = (rangeProperty != null) ? rangeProperty.getPropertyId() : (rangeConfig != null) ? rangeConfig.getPropertyId() : null;
                hasRangePropChanged = hasProviderPropertiesChanged(rangePropertyId, provider);

                String range2PropertyId = (range2Property != null) ? range2Property.getPropertyId() : (range2Config != null) ? range2Config.getPropertyId() : null;
                hasRange2PropChanged = hasProviderPropertiesChanged(range2PropertyId, provider);
                String startDatePropertyId = (startDateProperty != null) ? startDateProperty.getPropertyId() : (startDateConfig != null) ? startDateConfig.getPropertyId() : null;
                hasStartDatePropChanged = hasProviderPropertiesChanged(startDatePropertyId, provider);
                String endDatePropertyId = (endDateProperty != null) ? endDateProperty.getPropertyId() : (endDateConfig != null) ? endDateConfig.getPropertyId() : null;
                hasEndDatePropChanged = hasProviderPropertiesChanged(endDatePropertyId, provider);
                String sizePropertyId = (sizeProperty != null) ? sizeProperty.getPropertyId() : (sizeConfig != null) ? sizeConfig.getPropertyId() : null;
                hasSizePropChanged = hasProviderPropertiesChanged(sizePropertyId, provider);
                String donePropertyId = (doneProperty != null) ? doneProperty.getPropertyId() : (doneConfig != null) ? doneConfig.getPropertyId() : null;
                hasDonePropChanged = hasProviderPropertiesChanged(donePropertyId, provider);
				String progressPropertyId = (progressProperty != null) ? progressProperty.getPropertyId() : (progressConfig != null) ? progressConfig.getPropertyId() : null;
                hasProgressPropChanged = hasProviderPropertiesChanged(progressPropertyId, provider);

            } catch (Exception e) {
                throw new DataDisplayerInvalidConfiguration("Error during displayer initialization.", e);
            }
            if (hasDomainPropChanged && domainConfig != null) throw new DataDisplayerInvalidConfiguration("The current chart displayer domain property [" + domainConfig.getPropertyId() + "] is no longer available in data provider with code [" + provider.getCode() + "].");
            if (hasRangePropChanged && rangeConfig != null) throw new DataDisplayerInvalidConfiguration("The current chart displayer range property [" + rangeConfig.getPropertyId() + "] is no longer available in data provider with code [" + provider.getCode() + "].");

            if (hasRange2PropChanged && range2Config != null) throw new DataDisplayerInvalidConfiguration("The current chart displayer range2 property [" + range2Config.getPropertyId() + "] is no longer available in data provider with code [" + provider.getCode() + "].");
            if (hasStartDatePropChanged && startDateConfig != null) throw new DataDisplayerInvalidConfiguration("The current chart displayer startDate property [" + startDateConfig.getPropertyId() + "] is no longer available in data provider with code [" + provider.getCode() + "].");
            if (hasEndDatePropChanged && endDateConfig != null) throw new DataDisplayerInvalidConfiguration("The current chart displayer endDate property [" + endDateConfig.getPropertyId() + "] is no longer available in data provider with code [" + provider.getCode() + "].");
            if (hasSizePropChanged && sizeConfig != null) throw new DataDisplayerInvalidConfiguration("The current chart displayer size property [" + sizeConfig.getPropertyId() + "] is no longer available in data provider with code [" + provider.getCode() + "].");
            if (hasDonePropChanged && doneConfig != null) throw new DataDisplayerInvalidConfiguration("The current chart displayer done property [" + doneConfig.getPropertyId() + "] is no longer available in data provider with code [" + provider.getCode() + "].");
            if (hasProgressPropChanged && progressConfig != null) throw new DataDisplayerInvalidConfiguration("The current chart displayer progress property [" + progressConfig.getPropertyId() + "] is no longer available in data provider with code [" + provider.getCode() + "].");
        }
    }

    /**
     * Check if a data provider property match with the serialized in the displayer.
     * @param propertyId The data property identifier of this displayer to check against data provider properties.
     * @param dataProvider The current data provider definition.
     * @return If the data displayer property exists in current data provider.
     */
    public boolean hasProviderPropertiesChanged(String propertyId, DataProvider dataProvider) throws Exception{
        if (propertyId == null) return false;
        
        DataSet dataSet = dataProvider.getDataSet();
        DataProperty[] datasetProperties = dataSet.getProperties();
        if (datasetProperties != null && datasetProperties.length > 0) {
            for (DataProperty datasetProperty : datasetProperties) {
                String datasetPropertyId = datasetProperty.getPropertyId();
                if (datasetPropertyId.equals(propertyId)) return false;
            }
        }
        return true;
    }
    
    /**
     * Get the property selected as the domain.
     */
    public DataProperty getDomainProperty() {
        try {
            // Get the domain property. Be aware of both property removal and data set refresh.
            DataSet dataSet = dataProvider.getDataSet();
            if (domainProperty == null || hasDataSetChanged(domainProperty)) {

                // If a domain is currently configured the try to get the property form that.
                if (domainConfig != null) domainProperty = dataSet.getPropertyById(domainConfig.getPropertyId());

                // If the property has been removed for any reason then reset the domain.
                if (domainProperty == null && domainConfig != null) domainConfig = null;
                if (domainProperty == null) domainProperty = getDomainPropertiesAvailable()[0];

                // Create a copy of the domain property to avoid changes to the original data set.
                domainProperty = domainProperty.cloneProperty();

                // If a domain config exists then apply it to the domain.
                if (domainConfig != null) domainConfig.apply(domainProperty);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return domainProperty;
    }

    public void setDomainProperty(DataProperty property) {
        domainProperty = property;
        if (domainProperty == null) domainConfig = null;
    }
	
	public DataProperty getStartDateProperty() {
        try {
            // Get the domain property. Be aware of both property removal and data set refresh.
            DataSet dataSet = dataProvider.getDataSet();
            if (startDateProperty == null || hasDataSetChanged(startDateProperty)) {

                // If a domain is currently configured the try to get the property form that.
                if (startDateConfig != null) startDateProperty = dataSet.getPropertyById(startDateConfig.getPropertyId());

                // If the property has been removed for any reason then reset the domain.
                if (startDateProperty == null && startDateConfig != null) startDateConfig = null;
                if (startDateProperty == null) startDateProperty = getDomainPropertiesAvailable()[0];

                // Create a copy of the domain property to avoid changes to the original data set.
                startDateProperty = startDateProperty.cloneProperty();

                // If a domain config exists then apply it to the domain.
                if (startDateConfig != null) startDateConfig.apply(startDateProperty);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return startDateProperty;
    }

    public void setStartDateProperty(DataProperty property) {
        startDateProperty = property;
        if (startDateProperty == null) startDateConfig = null;
    }
	
	public DataProperty getEndDateProperty() {
        try {
            // Get the domain property. Be aware of both property removal and data set refresh.
            DataSet dataSet = dataProvider.getDataSet();
            if (endDateProperty == null || hasDataSetChanged(endDateProperty)) {

                // If a domain is currently configured the try to get the property form that.
                if (endDateConfig != null) endDateProperty = dataSet.getPropertyById(endDateConfig.getPropertyId());

                // If the property has been removed for any reason then reset the domain.
                if (endDateProperty == null && endDateConfig != null) endDateConfig = null;
                if (endDateProperty == null) endDateProperty = getDomainPropertiesAvailable()[0];

                // Create a copy of the domain property to avoid changes to the original data set.
                endDateProperty = endDateProperty.cloneProperty();

                // If a domain config exists then apply it to the domain.
                if (endDateConfig != null) endDateConfig.apply(endDateProperty);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return endDateProperty;
    }

    public void setEndDateProperty(DataProperty property) {
        endDateProperty = property;
        if (endDateProperty == null) endDateConfig = null;
    }
	
	public DataProperty getSizeProperty() {
        try {
            // Get the domain property. Be aware of both property removal and data set refresh.
            DataSet dataSet = dataProvider.getDataSet();
            if (sizeProperty == null || hasDataSetChanged(sizeProperty)) {

                // If a domain is currently configured the try to get the property form that.
                if (sizeConfig != null) sizeProperty = dataSet.getPropertyById(sizeConfig.getPropertyId());

                // If the property has been removed for any reason then reset the domain.
                if (sizeProperty == null && sizeConfig != null) sizeConfig = null;
                if (sizeProperty == null) sizeProperty = getDomainPropertiesAvailable()[0];

                // Create a copy of the domain property to avoid changes to the original data set.
                sizeProperty = sizeProperty.cloneProperty();

                // If a domain config exists then apply it to the domain.
                if (sizeConfig != null) sizeConfig.apply(sizeProperty);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return sizeProperty;
    }

    public void setSizeProperty(DataProperty property) {
        sizeProperty = property;
        if (sizeProperty == null) sizeConfig = null;
    }
	
	public DataProperty getDoneProperty() {
        try {
            // Get the domain property. Be aware of both property removal and data set refresh.
            DataSet dataSet = dataProvider.getDataSet();
            if (doneProperty == null || hasDataSetChanged(doneProperty)) {

                // If a domain is currently configured the try to get the property form that.
                if (doneConfig != null) doneProperty = dataSet.getPropertyById(doneConfig.getPropertyId());

                // If the property has been removed for any reason then reset the domain.
                if (doneProperty == null && doneConfig != null) doneConfig = null;
                if (doneProperty == null) doneProperty = getDomainPropertiesAvailable()[0];

                // Create a copy of the domain property to avoid changes to the original data set.
                doneProperty = doneProperty.cloneProperty();

                // If a domain config exists then apply it to the domain.
                if (doneConfig != null) doneConfig.apply(doneProperty);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return doneProperty;
    }

    public void setDoneProperty(DataProperty property) {
        doneProperty = property;
        if (doneProperty == null) doneConfig = null;
    }
	
	public DataProperty getProgressProperty() {
        try {
            // Get the domain property. Be aware of both property removal and data set refresh.
            DataSet dataSet = dataProvider.getDataSet();
            if (progressProperty == null || hasDataSetChanged(progressProperty)) {

                // If a domain is currently configured the try to get the property form that.
                if (progressConfig != null) progressProperty = dataSet.getPropertyById(progressConfig.getPropertyId());

                // If the property has been removed for any reason then reset the domain.
                if (progressProperty == null && progressConfig != null) progressConfig = null;
                if (progressProperty == null) progressProperty = getDomainPropertiesAvailable()[0];

                // Create a copy of the domain property to avoid changes to the original data set.
                progressProperty = progressProperty.cloneProperty();

                // If a domain config exists then apply it to the domain.
                if (progressConfig != null) progressConfig.apply(progressProperty);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return progressProperty;
    }

    public void setProgressProperty(DataProperty property) {
        progressProperty = property;
        if (progressProperty == null) progressConfig = null;
    }

    /**
     * Get the property selected as the range.
     */
    public DataProperty getRangeProperty() {
        try {
            // Get the range property. Be aware of both property removal and data set refresh.
            DataSet dataSet = dataProvider.getDataSet();
            if (rangeProperty == null || hasDataSetChanged(rangeProperty)) {

                // If a range is currently configured then try to get the property from that.
                if (rangeConfig != null) rangeProperty = dataSet.getPropertyById(rangeConfig.getPropertyId());

                // If the property has been removed for any reason then reset the range.
                if (rangeProperty == null && rangeConfig != null) rangeConfig = null;
                if (rangeProperty == null) rangeProperty = getRangePropertiesAvailable()[0];

                // Create a copy of the property to avoid changes to the original data set.
                rangeProperty = rangeProperty.cloneProperty();

                // If a range config exists then apply it to the range.
                if (rangeConfig != null) {
                    rangeConfig.apply(rangeProperty);
                    rangeScalarFunction = DataDisplayerServices.lookup().getScalarFunctionManager().getScalarFunctionByCode(rangeConfig.getScalarFunctionCode());
                    unitI18nMap = new HashMap<Locale, String>(rangeConfig.getUnitI18nMap());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return rangeProperty;
    }
	
	/**
     * Get the property selected as the range.
     */
    public DataProperty getRange2Property() {
        try {
            // Get the range property. Be aware of both property removal and data set refresh.
            DataSet dataSet = dataProvider.getDataSet();
            if (range2Property == null || hasDataSetChanged(range2Property)) {

                // If a range is currently configured then try to get the property from that.
                if (range2Config != null) range2Property = dataSet.getPropertyById(range2Config.getPropertyId());

                // If the property has been removed for any reason then reset the range.
                if (range2Property == null && range2Config != null) range2Config = null;
                if (range2Property == null) range2Property = getRangePropertiesAvailable()[0];

                // Create a copy of the property to avoid changes to the original data set.
                range2Property = range2Property.cloneProperty();

                // If a range config exists then apply it to the range.
                if (range2Config != null) {
                    range2Config.apply(range2Property);
                    range2ScalarFunction = DataDisplayerServices.lookup().getScalarFunctionManager().getScalarFunctionByCode(range2Config.getScalarFunctionCode());
                    unitI18nMap = new HashMap<Locale, String>(range2Config.getUnitI18nMap());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return range2Property;
    }

    public void setRangeProperty(DataProperty property) {
        rangeProperty = property;
        rangeScalarFunction = new CountFunction();
        if (rangeProperty == null) rangeConfig = null;
    }
	
	public void setRange2Property(DataProperty property) {
        range2Property = property;
        range2ScalarFunction = new CountFunction();
        if (range2Property == null) range2Config = null;
    }

    public ScalarFunction getRangeScalarFunction() {
        if (rangeScalarFunction != null) return rangeScalarFunction;
        return rangeScalarFunction = new CountFunction();
    }
	
	public ScalarFunction getRange2ScalarFunction() {
        if (range2ScalarFunction != null) return range2ScalarFunction;
        return range2ScalarFunction = new CountFunction();
    }

    public void setRangeScalarFunction(ScalarFunction rangeScalarFunction) {
        this.rangeScalarFunction = rangeScalarFunction;
    }
	
	public void setRange2ScalarFunction(ScalarFunction range2ScalarFunction) {
        this.range2ScalarFunction = range2ScalarFunction;
    }

    public Map<Locale, String> getUnitI18nMap() {
        return unitI18nMap;
    }

    public void setUnitI18nMap(Map<Locale, String> unitI18nMap) {
        this.unitI18nMap.clear();
        this.unitI18nMap.putAll(unitI18nMap);
    }

    public String getUnit(Locale l) {
        Object result = LocaleManager.lookup().localize(unitI18nMap);
        if (result == null) result = UNIT_VALUE_TAG;
        return (String) result;
    }

    public void setUnit(String unit, Locale l) {
        unitI18nMap.put(l, unit);
    }

    public String getType() {
        List<String> types = getDataDisplayerRenderer().getAvailableChartTypes(this);
        if (StringUtils.isBlank(type) || !types.contains(type)) {
            type = getDataDisplayerRenderer().getDefaultChartType(this);
        }
        return type;
    }

    public void setType(String type) {
        List<String> types = getDataDisplayerRenderer().getAvailableChartTypes(this);
        if (types.contains(type)) {
            this.type = type;
        }
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
	
	public String getColor2() {
        return color2;
    }

    public void setColor2(String color2) {
        this.color2 = color2;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isShowLegend() {
        return showLegend;
    }

    public void setShowLegend(boolean showLegend) {
        this.showLegend = showLegend;
    }
	
	public boolean isDisableDrillDown() {
        return disableDrillDown;
    }

    public void setDisableDrillDown(boolean disableDrillDown) {
        this.disableDrillDown = disableDrillDown;
    }
	
	public boolean isUseProgressColumns() {
        return useProgressColumns;
    }

    public void setUseProgressColumns(boolean useProgressColumns) {
        this.useProgressColumns = useProgressColumns;
    }

    public boolean isAxisInteger() {
        return axisInteger;
    }

	public boolean isFixedColor() {
        return fixedColor;
    }
	
    public void setAxisInteger(boolean axisInteger) {
        this.axisInteger = axisInteger;
    }
	
	public void setFixedColor(boolean fixedColor) {
        this.fixedColor = fixedColor;
    }

    public String getLegendAnchor() {
        return legendAnchor;
    }

    public void setLegendAnchor(String legendAnchor) {
        this.legendAnchor = legendAnchor;
    }

    public boolean isShowTitle() {
        return showTitle;
    }

    public void setShowTitle(boolean showTitle) {
        this.showTitle = showTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGraphicAlign() {
        return graphicAlign;
    }

    public void setGraphicAlign(String graphicAlign) {
        this.graphicAlign = graphicAlign;
    }

    public int getIntervalsSortCriteria() {
        return intervalsSortCriteria;
    }

    public void setIntervalsSortCriteria(int intervalsSortCriteria) {
        this.intervalsSortCriteria = intervalsSortCriteria;
    }

    public int getIntervalsSortOrder() {
        return intervalsSortOrder;
    }

    public void setIntervalsSortOrder(int intervalsSortOrder) {
        this.intervalsSortOrder = intervalsSortOrder;
    }

    public int getMarginLeft() {
        return marginLeft;
    }

    public void setMarginLeft(int marginLeft) {
        this.marginLeft = marginLeft;
    }

    public int getMarginRight() {
        return marginRight;
    }

    public void setMarginRight(int marginRight) {
        this.marginRight = marginRight;
    }

    public int getMarginBottom() {
        return marginBottom;
    }

    public void setMarginBottom(int marginBottom) {
        this.marginBottom = marginBottom;
    }

    public int getMarginTop() {
        return marginTop;
    }

    public void setMarginTop(int marginTop) {
        this.marginTop = marginTop;
    }
	
	public int getLabelThreshold() {
        return labelThreshold;
    }

    public void setLabelThreshold(int labelThreshold) {
        this.labelThreshold = labelThreshold;
    }

    public boolean isShowLabelsXAxis() {
        return showLabelsXAxis;
    }

    public void setShowLabelsXAxis(boolean showLabelsXAxis) {
        this.showLabelsXAxis = showLabelsXAxis;
    }

    public DataSet buildXYDataSet() {
        DataProperty domainProperty = getDomainProperty();
        DataProperty rangeProperty = getRangeProperty();
        DataProperty range2Property = getRange2Property();
        ScalarFunction scalarFunction = getRangeScalarFunction();
        ScalarFunction scalar2Function = getRange2ScalarFunction();
        DataSet sourceDataSet = domainProperty.getDataSet();
		
		DataProperty startDateProperty = getStartDateProperty();
		DataProperty endDateProperty = getEndDateProperty();
		DataProperty sizeProperty = getSizeProperty();
		DataProperty doneProperty = getDoneProperty();
		DataProperty progressProperty = getProgressProperty();
		
        CodeBlockTrace trace = new BuildXYDataSetTrace(domainProperty, rangeProperty, scalarFunction).begin();
        try {
            if (domainProperty == null || domainProperty.getDomain() == null) return null;
            if (rangeProperty == null || scalarFunction == null) return null;

            // Group the original data set by the domain property.
            int pivot = sourceDataSet.getPropertyColumn(domainProperty);
            int range = sourceDataSet.getPropertyColumn(rangeProperty);
            int[] columns;
			String[] functionCodes;
			
			if(isUseProgressColumns()){
				int startDate = sourceDataSet.getPropertyColumn(startDateProperty);
				int endDate = sourceDataSet.getPropertyColumn(endDateProperty);
				int size = sourceDataSet.getPropertyColumn(sizeProperty);
				int done = sourceDataSet.getPropertyColumn(doneProperty);
				int progress = sourceDataSet.getPropertyColumn(progressProperty);
				
				if(range2Property != null){
					int range2 = sourceDataSet.getPropertyColumn(range2Property);
					columns = new int[] {pivot, range, range2, startDate, endDate, size, done, progress};
					functionCodes = new String[] {CountFunction.CODE, scalar2Function.getCode(), scalarFunction.getCode()};
				}
				else{
					columns = new int[] {pivot, range, startDate, endDate, size, done, progress};
					functionCodes = new String[] {CountFunction.CODE, scalarFunction.getCode()};
				}
			}
			else if(range2Property != null){
				int range2 = sourceDataSet.getPropertyColumn(range2Property);
				columns = new int[] {pivot, range, range2};
				functionCodes = new String[] {CountFunction.CODE, scalarFunction.getCode(), scalar2Function.getCode()};
			}
			else{
				columns = new int[] {pivot, range};
				functionCodes = new String[] {CountFunction.CODE, scalarFunction.getCode()};
			}
            
            return sourceDataSet.groupBy(domainProperty, columns, functionCodes, intervalsSortCriteria, intervalsSortOrder);
        } finally {
            trace.end();
        }
    }

    public void copyFrom(DataDisplayer sourceDisplayer) throws DataDisplayerInvalidConfiguration {
        try {
            super.copyFrom(sourceDisplayer);

            AbstractChartDisplayer source = (AbstractChartDisplayer) sourceDisplayer;
            setBackgroundColor(source.getBackgroundColor());
            setColor(source.getColor());
            setColor2(source.getColor2());
            setDomainConfiguration(source.domainConfig);
            setDomainProperty(source.getDomainProperty());
            setGraphicAlign(source.getGraphicAlign());
            setHeight(source.getHeight());
            setLegendAnchor(source.getLegendAnchor());
            setRangeConfiguration(source.rangeConfig);
            setRange2Configuration(source.range2Config);
            setRangeProperty(source.getRangeProperty());
            setRange2Property(source.getRange2Property());
            setRangeScalarFunction(source.getRangeScalarFunction());
            setRange2ScalarFunction(source.getRange2ScalarFunction());
			
			setStartDateProperty(source.getStartDateProperty());
			setEndDateProperty(source.getEndDateProperty());
			setSizeProperty(source.getSizeProperty());
			setDoneProperty(source.getDoneProperty());
			setProgressProperty(source.getProgressProperty());
			
            setMarginBottom(source.getMarginBottom());
            setMarginTop(source.getMarginTop());
            setMarginLeft(source.getMarginLeft());
            setMarginRight(source.getMarginRight());
            setLabelThreshold(source.getLabelThreshold());
            setTitle(source.getTitle());
            setWidth(source.getWidth());
            setAxisInteger(source.isAxisInteger());
            setFixedColor(source.isFixedColor());
            setShowLegend(source.isShowLegend());
            setDisableDrillDown(source.isDisableDrillDown());
            setShowTitle(source.isShowTitle());
            setIntervalsSortCriteria(source.getIntervalsSortCriteria());
            setIntervalsSortOrder(source.getIntervalsSortOrder());
            setShowLabelsXAxis(source.isShowLabelsXAxis());
        } catch (ClassCastException e) {
            // Ignore wrong types
        }
    }

    public void setDomainConfiguration(DomainConfiguration config) {
        domainConfig = config;
    }

    public void setRangeConfiguration(RangeConfiguration config) {
        rangeConfig = config;
    }
	
	public void setRange2Configuration(RangeConfiguration config) {
        range2Config = config;
    }

    public DomainConfiguration getDomainConfiguration() {
        return domainConfig;
    }

    public RangeConfiguration getRangeConfiguration() {
        return rangeConfig;
    }
	
	public RangeConfiguration getRange2Configuration() {
        return range2Config;
    }
	
	public void setStartDateConfiguration(DomainConfiguration config) {
        startDateConfig = config;
    }
	
	public DomainConfiguration getStartDateConfiguration() {
        return startDateConfig;
    }
	
	public void setEndDateConfiguration(DomainConfiguration config) {
        endDateConfig = config;
    }
	
	public DomainConfiguration getEndDateConfiguration() {
        return endDateConfig;
    }
	
	public void setSizeConfiguration(DomainConfiguration config) {
        sizeConfig = config;
    }
	
	public DomainConfiguration getSizeConfiguration() {
        return sizeConfig;
    }
	
	public void setDoneConfiguration(DomainConfiguration config) {
        doneConfig = config;
    }
	
	public DomainConfiguration getDoneConfiguration() {
        return doneConfig;
    }
	
	public void setProgressConfiguration(DomainConfiguration config) {
        progressConfig = config;
    }
	
	public DomainConfiguration getProgressConfiguration() {
        return progressConfig;
    }

    class BuildXYDataSetTrace extends CodeBlockTrace {

        protected String displayerTitle;
        protected String providerCode;
        protected String scalarFunctionCode;
        protected String domainPropId;
        protected String rangePropId;

        public BuildXYDataSetTrace(DataProperty domainProperty, DataProperty rangeProperty, ScalarFunction scalarFunction) {
            super(null);
            displayerTitle = getTitle();
            DataProvider dataProvider = domainProperty.getDataSet().getDataProvider();
            providerCode = dataProvider.getCode();
            scalarFunctionCode = scalarFunction.getCode();
            domainPropId = domainProperty.getPropertyId();
            rangePropId = rangeProperty.getPropertyId();
            setId(providerCode + "-" + scalarFunctionCode + "-" + rangePropId + "-" + domainPropId);
        }

        public CodeBlockType getType() {
            return CoreCodeBlockTypes.DATASET_BUILD;
        }

        public String getDescription() {
            return domainPropId + " / " + scalarFunctionCode + "(" + rangePropId + ")";
        }

        public Map<String, Object> getContext() {
            Map<String, Object> ctx = new HashMap<String, Object>();
            ctx.put("Chart title", displayerTitle);
            ctx.put("Provider code", providerCode);
            ctx.put("Domain property", domainPropId);
            ctx.put("Range property", rangePropId);
            ctx.put("Scalar function", scalarFunctionCode);
            return ctx;
        }
    }
}
