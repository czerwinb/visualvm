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

package com.sun.tools.visualvm.application.views.monitor;

import com.sun.tools.visualvm.core.options.GlobalPreferences;
import org.netbeans.lib.profiler.ui.graphs.GraphPanel;
import org.netbeans.lib.profiler.ui.components.ColorIcon;
import org.netbeans.lib.profiler.ui.charts.ChartModelListener;
import org.netbeans.lib.profiler.ui.charts.SynchronousXYChart;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

/**
 *
 * @author Jiri Sedlacek
 * @author Tomas Hurka
 */
class ChartsSupport {
    
  public static final int MINIMUM_CHART_HEIGHT = 275;

  public static abstract class Chart extends GraphPanel implements ChartModelListener {

    private SynchronousXYChart xyChart;
    private BoundedDynamicXYChartModel xyChartModel;
    private JPanel bigLegendPanel;
    private JPanel smallLegendPanel;
    
    public Chart() {
      long time = System.currentTimeMillis();
      
      setLayout(new BorderLayout());
      setOpaque(true);
      setBackground(Color.WHITE);
      
      GlobalPreferences preferences = GlobalPreferences.sharedInstance();
      xyChartModel = new BoundedDynamicXYChartModel(preferences.getMonitoredDataCache() * 60 / preferences.getMonitoredDataPoll()) {
        public  long    getMaxDisplayYValue(int seriesIndex)      { return getMaxYValue(0); }
      };
      setupModel(xyChartModel);
      
      xyChart = createChart();
      xyChart.setBackgroundPaint(getBackground());
      xyChart.setModel(xyChartModel);
      xyChart.setFitToWindow();
      xyChart.setupInitialAppearance(time, time + 1200, 0, 2);
      add(xyChart, BorderLayout.CENTER);
      
      bigLegendPanel = createBigLegend();
      smallLegendPanel = createSmallLegend();
      
      ToolTipManager.sharedInstance().registerComponent(xyChart);
      
//      xyChartModel.addChartModelListener(this);
    }
    
    protected abstract SynchronousXYChart createChart();
    protected abstract void setupModel(BoundedDynamicXYChartModel xyChartModel);
    protected abstract JPanel createBigLegend();
    protected abstract JPanel createSmallLegend();
    
    public SynchronousXYChart getChart() {
      return xyChart;
    };
    
    public BoundedDynamicXYChartModel getModel() {
      return xyChartModel;
    }
    
    public JPanel getBigLegendPanel() {
      return bigLegendPanel;
    };
    
    public JPanel getSmallLegendPanel() {
      return smallLegendPanel;
    }
    
    public void chartDataChanged() {
//      if (xyChart.isFitToWindow() && xyChartModel.getMaxXValue() - xyChartModel.getMinXValue() >= chartTimeLength) { // after 3 minutes switch from fitToWindow to trackingEnd
//        xyChart.setTrackingEnd();
//      }
    }
    
    public void setToolTipText(String toolTipText) {
        super.setToolTipText(toolTipText);
        xyChart.setToolTipText(toolTipText);
    }
    
  }
  
  public static class ClassesMetricsChart extends Chart {
    
    protected void setupModel(BoundedDynamicXYChartModel xyChartModel) {
      xyChartModel.setupModel(new String[] {"Total loaded classes", "Shared loaded classes"}, new Color[] { new Color(255, 127, 127),new Color(127, 63, 191)} );
    }

    protected SynchronousXYChart createChart() {
      SynchronousXYChart xyChart = new SynchronousXYChart(SynchronousXYChart.TYPE_LINE, SynchronousXYChart.VALUES_INTERPOLATED, 0.01);
      xyChart.setTopChartMargin(20);
      xyChart.denySelection();
      xyChart.setMinimumVerticalMarksDistance(UIManager.getFont("Panel.font").getSize() + 8); // NOI18N
      return xyChart;
    }

    protected JPanel createBigLegend() {
      JLabel totalClasses = new JLabel(getModel().getSeriesName(0), new ColorIcon(getModel().getSeriesColor(0), Color.BLACK, 18, 9), SwingConstants.LEADING);
      totalClasses.setOpaque(false);
      totalClasses.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
      JLabel sharedClasses = new JLabel(getModel().getSeriesName(1), new ColorIcon(getModel().getSeriesColor(1), Color.BLACK, 18, 9), SwingConstants.LEADING);
      sharedClasses.setOpaque(false);
      sharedClasses.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

      JPanel legendPanel = new JPanel();
      legendPanel.setOpaque(false);
      legendPanel.add(totalClasses);
      legendPanel.add(sharedClasses);
      
      return legendPanel;
    }

    protected JPanel createSmallLegend() {
      return null;
    }
    
  }
  
  public static class ThreadsMetricsChart extends Chart {
    
    protected void setupModel(BoundedDynamicXYChartModel xyChartModel) {
      xyChartModel.setupModel(new String[] {"Live threads", "Daemon threads"}, new Color[] { new Color(255, 127, 127),new Color(127, 63, 191)} );
    }

    protected SynchronousXYChart createChart() {
      SynchronousXYChart xyChart = new SynchronousXYChart(SynchronousXYChart.TYPE_LINE, SynchronousXYChart.VALUES_INTERPOLATED, 0.01);
      xyChart.setTopChartMargin(20);
      xyChart.denySelection();
      xyChart.setMinimumVerticalMarksDistance(UIManager.getFont("Panel.font").getSize() + 8); // NOI18N
      return xyChart;
    }

    protected JPanel createBigLegend() {
      JLabel liveThreads = new JLabel(getModel().getSeriesName(0), new ColorIcon(getModel().getSeriesColor(0), Color.BLACK, 18, 9), SwingConstants.LEADING);
      liveThreads.setOpaque(false);
      liveThreads.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
      JLabel daemonThreads = new JLabel(getModel().getSeriesName(1), new ColorIcon(getModel().getSeriesColor(1), Color.BLACK, 18, 9), SwingConstants.LEADING);
      daemonThreads.setOpaque(false);
      daemonThreads.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

      JPanel legendPanel = new JPanel();
      legendPanel.setOpaque(false);
      legendPanel.add(liveThreads);
      legendPanel.add(daemonThreads);
      
      return legendPanel;
    }

    protected JPanel createSmallLegend() {
      return null;
    }
    
  }
  
  
  public static class HeapMetricsChart extends Chart {
    
    protected void setupModel(BoundedDynamicXYChartModel xyChartModel) {
      xyChartModel.setupModel(new String[] {"Heap size", "Used heap"}, new Color[] { new Color(255, 127, 127),new Color(127, 63, 191)} );
    }

    protected SynchronousXYChart createChart() {
      SynchronousXYChart xyChart = new SynchronousXYChart(SynchronousXYChart.TYPE_FILL, SynchronousXYChart.VALUES_INTERPOLATED, 0.01);
      xyChart.setVerticalAxisValueDivider(1024*1024);
      xyChart.setVerticalAxisValueString("M"); // NOI18N
      xyChart.setTopChartMargin(20);
      xyChart.denySelection();
      xyChart.setMinimumVerticalMarksDistance(UIManager.getFont("Panel.font").getSize() + 8); // NOI18N
      return xyChart;
    }

    protected JPanel createBigLegend() {
      JLabel liveThreads = new JLabel(getModel().getSeriesName(0), new ColorIcon(getModel().getSeriesColor(0), Color.BLACK, 18, 9), SwingConstants.LEADING);
      liveThreads.setOpaque(false);
      liveThreads.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
      JLabel daemonThreads = new JLabel(getModel().getSeriesName(1), new ColorIcon(getModel().getSeriesColor(1), Color.BLACK, 18, 9), SwingConstants.LEADING);
      daemonThreads.setOpaque(false);
      daemonThreads.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

      JPanel legendPanel = new JPanel();
      legendPanel.setOpaque(false);
      legendPanel.add(liveThreads);
      legendPanel.add(daemonThreads);
      
      return legendPanel;
    }

    protected JPanel createSmallLegend() {
      return null;
    }
    
  }
  
  public static class PermGenMetricsChart extends Chart {
    
    protected void setupModel(BoundedDynamicXYChartModel xyChartModel) {
      xyChartModel.setupModel(new String[] {"PermGen size", "Used PermGen"}, new Color[] { new Color(255, 127, 127),new Color(127, 63, 191)} );
    }

    protected SynchronousXYChart createChart() {
      SynchronousXYChart xyChart = new SynchronousXYChart(SynchronousXYChart.TYPE_FILL, SynchronousXYChart.VALUES_INTERPOLATED, 0.01);
      xyChart.setVerticalAxisValueDivider(1024*1024);
      xyChart.setVerticalAxisValueString("M"); // NOI18N
      xyChart.setTopChartMargin(20);
      xyChart.denySelection();
      xyChart.setMinimumVerticalMarksDistance(UIManager.getFont("Panel.font").getSize() + 8); // NOI18N
      return xyChart;
    }

    protected JPanel createBigLegend() {
      JLabel liveThreads = new JLabel(getModel().getSeriesName(0), new ColorIcon(getModel().getSeriesColor(0), Color.BLACK, 18, 9), SwingConstants.LEADING);
      liveThreads.setOpaque(false);
      liveThreads.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
      JLabel daemonThreads = new JLabel(getModel().getSeriesName(1), new ColorIcon(getModel().getSeriesColor(1), Color.BLACK, 18, 9), SwingConstants.LEADING);
      daemonThreads.setOpaque(false);
      daemonThreads.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

      JPanel legendPanel = new JPanel();
      legendPanel.setOpaque(false);
      legendPanel.add(liveThreads);
      legendPanel.add(daemonThreads);
      
      return legendPanel;
    }

    protected JPanel createSmallLegend() {
      return null;
    }
    
  }
  
}
