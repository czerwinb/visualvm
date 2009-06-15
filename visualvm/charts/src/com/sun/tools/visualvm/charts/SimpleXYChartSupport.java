/*
 * Copyright 2007-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.tools.visualvm.charts;

import com.sun.tools.visualvm.charts.xy.SimpleXYChartUtils;
import com.sun.tools.visualvm.charts.xy.XYPaintersModel;
import com.sun.tools.visualvm.charts.xy.XYStorage;
import java.awt.Color;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItemsModel;

/**
 * Handle to access a simple XY chart. The chart supports adding new data and
 * updating the details area.
 *
 * @author Jiri Sedlacek
 */
public final class SimpleXYChartSupport {

    // --- Public chart boundary constants -------------------------------------

    public static final long MIN_UNDEFINED = Long.MAX_VALUE;
    public static final long MAX_UNDEFINED = Long.MIN_VALUE;


    // --- Instance variables --------------------------------------------------

    private final int chartType;
    private final boolean hideItems;
    private final Color[] itemColors;
    private final long initialYMargin;
    private final String[] detailsItems;

    private final XYStorage storage;
    private final SynchronousXYItemsModel itemsModel;
    private final XYPaintersModel paintersModel;
    
    private JComponent chartUI;
    private SimpleXYChartUtils.DetailsHandle detailsHandle;
    

    // --- Public interface ----------------------------------------------------

    /**
     * Returns a JComponent containing the chart.
     * <br><br><b>Note:</b> This method must be called in the Event Dispatch Thread.
     *
     * @return JComponent containing the chart;
     */
    public JComponent getChart() {
        if (chartUI == null) {
            chartUI = SimpleXYChartUtils.createChartUI(chartType,
                                                     itemColors, initialYMargin,
                                                     hideItems, storage,
                                                     itemsModel, paintersModel);
            if (detailsItems != null)
                detailsHandle = SimpleXYChartUtils.createDetailsArea(detailsItems,
                                                                     chartUI);
        }
        return chartUI;
    }


    /**
     * Adds a packet of values.
     * <br><br><b>Note:</b> This method can be called from any thread.
     *
     * @param timestamp timestamp of the data packet
     * @param values data packet
     */
    public void addValues(final long timestamp, final long[] values) {
        Runnable valuesUpdater = new Runnable() {
            public void run() {
                storage.addValues(timestamp, values);
                itemsModel.valuesAdded();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) valuesUpdater.run();
        else SwingUtilities.invokeLater(valuesUpdater);
    }

    /**
     * Updates the details area of the chart.
     * <br><br><b>Note:</b> This method can be called from any thread.
     *
     * @param details details packet
     */
    public void updateDetails(final String[] values) {
        Runnable detailsUpdater = new Runnable() {
            public void run() { detailsHandle.updateDetails(values); }
        };
        if (SwingUtilities.isEventDispatchThread()) detailsUpdater.run();
        else SwingUtilities.invokeLater(detailsUpdater);
    }


    /**
     * Formats a decimal value to String. Use this method to make sure the value
     * appears in the same format as used in the chart (tooltip). Useful for
     * updating details area.
     * <br><br><b>Note:</b> This method must be called in the Event Dispatch Thread.
     *
     * @param value value to be formatted
     * @return formatted value in the same format as used in the chart
     */
    public String formatDecimal(long value) {
        return SimpleXYChartUtils.formatDecimal(value);
    }

    /**
     * Formats a bytes value to String. Use this method to make sure the value
     * appears in the same format as used in the chart (tooltip). Useful for
     * updating details area.
     * <br><br><b>Note:</b> This method must be called in the Event Dispatch Thread.
     *
     * @param value value to be formatted
     * @return formatted value in the same format as used in the chart
     */
    public String formatBytes(long value) {
        return SimpleXYChartUtils.formatBytes(value);
    }

    /**
     * Formats a percent value to String. Use this method to make sure the value
     * appears in the same format as used in the chart (tooltip). Useful for
     * updating details area.
     * <br><br><b>Note:</b> This method must be called in the Event Dispatch Thread.
     *
     * @param value value to be formatted
     * @return formatted value in the same format as used in the chart
     */
    public String formatPercent(long value) {
        return SimpleXYChartUtils.formatPercent(value);
    }

    /**
     * Formats a time value to String. Use this method to make sure the value
     * appears in the same format as used in the chart (tooltip). Useful for
     * updating details area.
     * <br><br><b>Note:</b> This method must be called in the Event Dispatch Thread.
     *
     * @param value value to be formatted
     * @return formatted value in the same format as used in the chart
     */
    public String formatTime(long value) {
        int timestamps = storage.getTimestampsCount();
        if (timestamps == 0) return SimpleXYChartUtils.formatTime(value, value, value);
        else return SimpleXYChartUtils.formatTime(value, storage.getTimestamp(0),
                                                  storage.getTimestamp(timestamps - 1));
    }


    // --- Internal constructors -----------------------------------------------

    SimpleXYChartSupport(int chartType, long initialYMargin, String[] itemNames, Color[] itemColors,
                         float[] lineWidths, Color[] lineColors, Color[] fillColors1, Color[] fillColors2,
                         long minValue, long maxValue, boolean hideItems, int valuesBuffer,
                         String[] detailsItems) {

        this.chartType = chartType;
        this.hideItems = hideItems;
        this.itemColors = itemColors;
        this.initialYMargin = initialYMargin;
        this.detailsItems = detailsItems;
        
        storage = SimpleXYChartUtils.createStorage(valuesBuffer);
        itemsModel = SimpleXYChartUtils.createItemsModel(storage, itemNames, minValue, maxValue);
        paintersModel = SimpleXYChartUtils.createPaintersModel(lineWidths, lineColors,
                                            fillColors1, fillColors2, itemsModel);
    }

}
