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

package com.sun.tools.visualvm.profiler;

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.application.jvm.Jvm;
import com.sun.tools.visualvm.application.jvm.JvmFactory;
import com.sun.tools.visualvm.core.datasource.descriptor.DataSourceDescriptorFactory;
import com.sun.tools.visualvm.core.datasupport.DataRemovedListener;
import com.sun.tools.visualvm.core.datasupport.Stateful;
import com.sun.tools.visualvm.core.ui.DataSourceView;
import com.sun.tools.visualvm.core.ui.DesktopUtils;
import com.sun.tools.visualvm.core.ui.components.DataViewComponent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.text.MessageFormat;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.netbeans.lib.profiler.TargetAppRunner;
import org.netbeans.lib.profiler.common.AttachSettings;
import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.lib.profiler.common.event.ProfilingStateEvent;
import org.netbeans.lib.profiler.common.event.ProfilingStateListener;
import org.netbeans.lib.profiler.ui.components.HTMLLabel;
import org.netbeans.lib.profiler.ui.components.HTMLTextArea;
import org.netbeans.modules.profiler.LiveResultsWindow;
import org.netbeans.modules.profiler.NetBeansProfiler;
import org.netbeans.modules.profiler.utils.IDEUtils;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Jiri Sedlacek
 */
class ApplicationProfilerView extends DataSourceView {
    
    private static final String IMAGE_PATH = "com/sun/tools/visualvm/profiler/resources/profiler.png";  // NOI18N
    
    private MasterViewSupport masterViewSupport;
    private CPUSettingsSupport cpuSettingsSupport;
    private MemorySettingsSupport memorySettingsSupport;

    
    public ApplicationProfilerView(Application application) {
        super(application, NbBundle.getMessage(ApplicationProfilerView.class, "LBL_Profiler"), new ImageIcon(Utilities.loadImage(IMAGE_PATH, true)).getImage(), 40, false);    // NOI18N
        cpuSettingsSupport = new CPUSettingsSupport(application);
        memorySettingsSupport = new MemorySettingsSupport(application);
    }
        
    
    protected DataViewComponent createComponent() {
        Application application = (Application)getDataSource();
        ProfilingResultsSupport profilingResultsSupport = new ProfilingResultsSupport();
        masterViewSupport = new MasterViewSupport(application, profilingResultsSupport, cpuSettingsSupport, memorySettingsSupport);
        
        DataViewComponent dvc = new DataViewComponent(
                masterViewSupport.getMasterView(),
                new DataViewComponent.MasterViewConfiguration(false));
        
        dvc.configureDetailsArea(new DataViewComponent.DetailsAreaConfiguration(NbBundle.getMessage(ApplicationProfilerView.class, "LBL_Profiling_results"), false), DataViewComponent.TOP_LEFT);   // NOI18N
        dvc.addDetailsView(profilingResultsSupport.getDetailsView(), DataViewComponent.TOP_LEFT);
        
        dvc.configureDetailsArea(new DataViewComponent.DetailsAreaConfiguration(NbBundle.getMessage(ApplicationProfilerView.class, "LBL_Settings"), true), DataViewComponent.TOP_RIGHT);   // NOI18N
        dvc.addDetailsView(cpuSettingsSupport.getDetailsView(), DataViewComponent.TOP_RIGHT);
        dvc.addDetailsView(memorySettingsSupport.getDetailsView(), DataViewComponent.TOP_RIGHT);
        dvc.hideDetailsArea(DataViewComponent.TOP_RIGHT);
        
        cpuSettingsSupport = null;
        memorySettingsSupport = null;
        
        return dvc;
    }
    
    protected void removed() {
        masterViewSupport.viewRemoved();
    }
    
    
    // --- General data --------------------------------------------------------
    
    private static class MasterViewSupport extends JPanel implements ProfilingStateListener, DataRemovedListener<Application>, ActionListener {
        
        private Application application;
        private ProfilingResultsSupport profilingResultsView;
        private CPUSettingsSupport cpuSettingsSupport;
        private MemorySettingsSupport memorySettingsSupport;
        private AttachSettings attachSettings;
        private Timer timer;
        private int lastInstrValue = -1;

        private int state = -1;

        private boolean internalChange = false;
    
        
        public MasterViewSupport(Application application, ProfilingResultsSupport profilingResultsView,
                CPUSettingsSupport cpuSettingsSupport, MemorySettingsSupport memorySettingsSupport) {
            this.application = application;
            this.profilingResultsView = profilingResultsView;
            this.cpuSettingsSupport = cpuSettingsSupport;
            this.memorySettingsSupport = memorySettingsSupport;
            
            initComponents();
            initSettings();
            refreshStatus();
            
            timer = new Timer(1000, this);
            timer.setInitialDelay(1000);

            NetBeansProfiler.getDefaultNB().addProfilingStateListener(this);
            
            // TODO: should listen for PROPERTY_AVAILABLE instead of DataSource removal
            application.notifyWhenRemoved(this);
        }
        
        
        public DataViewComponent.MasterView getMasterView() {
            return new DataViewComponent.MasterView(NbBundle.getMessage(ApplicationProfilerView.class, "LBL_Profiler"), null, this);    // NOI18N
        }
        
        public void dataRemoved(Application application) {
            timer.stop();
            timer.removeActionListener(MasterViewSupport.this);
            NetBeansProfiler.getDefaultNB().removeProfilingStateListener(MasterViewSupport.this);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    disableControlButtons();
                    statusValueLabel.setText(NbBundle.getMessage(ApplicationProfilerView.class, "MSG_application_terminated")); // NOI18N
                }
            });
        }
        
        public void viewRemoved() {
            timer.stop();
            timer.removeActionListener(MasterViewSupport.this);
            NetBeansProfiler.getDefaultNB().removeProfilingStateListener(MasterViewSupport.this);
        }
        
        public void actionPerformed(ActionEvent e) {
            updateRunningText();
        }
        
        
        private static JComponent getLiveResultsView() {
            JComponent view = LiveResultsWindow.getDefault();
            view.setPreferredSize(new Dimension(1, 1));
            return view;
        }
        
        private void handleCPUProfiling() {
          if (internalChange) return;

          if (cpuButton.isSelected())  {
            internalChange = true;
            memoryButton.setSelected(false);
            internalChange = false;
            if (!cpuSettingsSupport.isRootValueValid() ||
                    !cpuSettingsSupport.isFilterValueValid()) {
                internalChange = true;
                cpuButton.setSelected(false);
                internalChange = false;
                NetBeansProfiler.getDefaultNB().displayError(NbBundle.getMessage(ApplicationProfilerView.class, "MSG_Incorrect_CPU_settings")); // NOI18N
            } else {
                cpuSettingsSupport.saveSettings();
                if (NetBeansProfiler.getDefaultNB().getProfilingState() == NetBeansProfiler.PROFILING_RUNNING) {
                  IDEUtils.runInProfilerRequestProcessor(new Runnable() {
                    public void run() { NetBeansProfiler.getDefaultNB().modifyCurrentProfiling(cpuSettingsSupport.getSettings()); }
                  });
                } else {
                  disableControlButtons();
                  ProfilerSupport.getInstance().setProfiledApplication(application);
                  IDEUtils.runInProfilerRequestProcessor(new Runnable() {
                    public void run() {
                      NetBeansProfiler.getDefaultNB().attachToApp(cpuSettingsSupport.getSettings(), attachSettings);
                    }
                  });
                }
            }
          }
        }

        private void handleMemoryProfiling() {
          if (internalChange) return;

          if (memoryButton.isSelected())  {
            internalChange = true;
            cpuButton.setSelected(false);
            internalChange = false;
            memorySettingsSupport.saveSettings();
            if (NetBeansProfiler.getDefaultNB().getProfilingState() == NetBeansProfiler.PROFILING_RUNNING) {
              IDEUtils.runInProfilerRequestProcessor(new Runnable() {
                public void run() { 
                    
                    NetBeansProfiler.getDefaultNB().modifyCurrentProfiling(memorySettingsSupport.getSettings()); 
                }
              });
            } else {
              disableControlButtons();
              ProfilerSupport.getInstance().setProfiledApplication(application);
              IDEUtils.runInProfilerRequestProcessor(new Runnable() {
                public void run() {
                  NetBeansProfiler.getDefaultNB().attachToApp(memorySettingsSupport.getSettings(), attachSettings);
                }
              });
            }
          }
        }

        private void handleStopProfiling() {
          if (internalChange) return;

          disableControlButtons();
          IDEUtils.runInProfilerRequestProcessor(new Runnable() {
            public void run() {
              ProfilerSupport.getInstance().setProfiledApplication(null);
              NetBeansProfiler.getDefaultNB().detachFromApp();
            }
          });
        }


        public void profilingStateChanged(ProfilingStateEvent e) { refreshStatus(); }
        public void threadsMonitoringChanged() { refreshStatus(); }
        public void instrumentationChanged(int oldInstrType, int currentInstrType) { refreshStatus(); }


        private void refreshStatus() {

          final int newState = NetBeansProfiler.getDefaultNB().getProfilingState();
          final Application profiledApplication = ProfilerSupport.getInstance().getProfiledApplication();
          if (state != newState) {
            state = newState;
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                switch (state) {
                  case NetBeansProfiler.PROFILING_INACTIVE:
                    ProfilerSupport.getInstance().setProfiledApplication(null); // Necessary to set here when profiled app finished
                    timer.stop();
                    lastInstrValue = -1;
                    statusValueLabel.setText(NbBundle.getMessage(ApplicationProfilerView.class, "MSG_profiling_inactive"));    // NOI18N
                    resetControlButtons();
                    RequestProcessor.getDefault().post(new Runnable() {
                      public void run() {
                          enableControlButtons();
                          enableSettings();
                      }
                    }, 500); // Wait for the application to finish
                    break;
                  case NetBeansProfiler.PROFILING_STARTED:
                    timer.stop();
                    statusValueLabel.setText(NbBundle.getMessage(ApplicationProfilerView.class, "MSG_profiling_started")); // NOI18N
                    break;
                  case NetBeansProfiler.PROFILING_PAUSED:
                    timer.stop();
                    statusValueLabel.setText(NbBundle.getMessage(ApplicationProfilerView.class, "MSG_profiling_paused"));   // NOI18N
                    break;
                  case NetBeansProfiler.PROFILING_IN_TRANSITION:
                    timer.stop();
                    statusValueLabel.setText(NbBundle.getMessage(ApplicationProfilerView.class, "MSG_refreshing")); // NOI18N
                    disableControlButtons();
                    disableSettings();
                    break;
                  case NetBeansProfiler.PROFILING_RUNNING:

                    if (application.equals(profiledApplication)) {
                      updateRunningText();
                      timer.start();
                      enableControlButtons();
                      updateControlButtons();
                      disableSettings();
                      profilingResultsView.setProfilingResultsDisplay(getLiveResultsView());
                    } else {
                      statusValueLabel.setText(NbBundle.getMessage(ApplicationProfilerView.class, "MSG_profiling_of") + DataSourceDescriptorFactory.getDescriptor(profiledApplication).getName() + NbBundle.getMessage(ApplicationProfilerView.class, "MSG_in_progress"));  // NOI18N
                      disableControlButtons();
                      profilingResultsView.setProfilingResultsDisplay(null);
                    }

                    profilingResultsView.revalidate();
                    profilingResultsView.repaint();
                    revalidate();
                    repaint();

                    break;
                  case NetBeansProfiler.PROFILING_STOPPED:
                    timer.stop();
                    statusValueLabel.setText(NbBundle.getMessage(ApplicationProfilerView.class, "MSG_profiling_stopped")); // NOI18N
                    break;
                }
              }
            });
          }
        }

        private void updateRunningText() {
            ProfilingSettings currentSettings = NetBeansProfiler.getDefaultNB().getLastProfilingSettings();
            int currentProfilingType = currentSettings != null ? currentSettings.getProfilingType() : Integer.MIN_VALUE;
            if (cpuSettingsSupport.getSettings().getProfilingType() == currentProfilingType) {
                int instrValue = TargetAppRunner.getDefault().getProfilingSessionStatus().getNInstrMethods();
                if (lastInstrValue != instrValue)
                    statusValueLabel.setText(MessageFormat.format(NbBundle.getMessage(ApplicationProfilerView.class,
                            "MSG_profiling_running_methods"), new Object[] { instrValue })); // NOI18N
                lastInstrValue = instrValue;
            } else if (memorySettingsSupport.getSettings().getProfilingType() == currentProfilingType) {
                int instrValue = TargetAppRunner.getDefault().getProfilingSessionStatus().getNInstrClasses();
                if (lastInstrValue != instrValue)
                    statusValueLabel.setText(MessageFormat.format(NbBundle.getMessage(ApplicationProfilerView.class,
                            "MSG_profiling_running_classes"), new Object[] { instrValue })); // NOI18N
                lastInstrValue = instrValue;
            }
        }
        
        private void enableSettings() {
            cpuSettingsSupport.setUIEnabled(true);
            memorySettingsSupport.setUIEnabled(true);
        }
        
        private void disableSettings() {
            cpuSettingsSupport.setUIEnabled(false);
            memorySettingsSupport.setUIEnabled(false);
        }

        private void resetControlButtons() {
          internalChange = true;
          cpuButton.setSelected(false);
          memoryButton.setSelected(false);
          internalChange = false;
        }
        
        private void updateControlButtons() {
            ProfilingSettings currentSettings = NetBeansProfiler.getDefaultNB().getLastProfilingSettings();
            int currentProfilingType = currentSettings != null ? currentSettings.getProfilingType() : Integer.MIN_VALUE;
            if (cpuSettingsSupport.getSettings().getProfilingType() == currentProfilingType && !cpuButton.isSelected()) {
                internalChange = true;
                cpuButton.setSelected(true);
                memoryButton.setSelected(false);
                internalChange = false;
            } else if (memorySettingsSupport.getSettings().getProfilingType() == currentProfilingType && !memoryButton.isSelected()) {
                internalChange = true;
                cpuButton.setSelected(false);
                memoryButton.setSelected(true);
                internalChange = false;
            }
        }

        private void enableControlButtons() {
          boolean enabled = application.getState() == Stateful.STATE_AVAILABLE;
          cpuButton.setEnabled(enabled);
          memoryButton.setEnabled(enabled);
          stopButton.setEnabled(NetBeansProfiler.getDefaultNB().getTargetAppRunner().targetAppIsRunning());
        }

        private void disableControlButtons() {
          cpuButton.setEnabled(false);
          memoryButton.setEnabled(false);
          stopButton.setEnabled(false);
        }


        private void initSettings() {
          // Attach settings default
          attachSettings = new AttachSettings();
          attachSettings.setDirect(false);
          attachSettings.setDynamic16(true);
          attachSettings.setPid(application.getPid());
        }
        
        
        private void initComponents() {
            setLayout(new BorderLayout());
            
            JPanel controlPanel = new JPanel();
              controlPanel.setOpaque(false);
              controlPanel.setLayout(new GridBagLayout());
              controlPanel.setBorder(BorderFactory.createEmptyBorder(6, 0, 3, 0));

              GridBagConstraints constraints;

              // classShareWarningLabel
              Jvm jvm = JvmFactory.getJVMFor(application);
              String vmInfo = jvm.getVmInfo();
              String vmVersion = jvm.getVmVersion();
              boolean classSharingBreaksProfiling = vmInfo.contains("sharing") && !vmVersion.equals("10.0-b23");    // NOI18N
              classShareWarningArea = new HTMLTextArea() {
                  protected void showURL(URL url) { 
                      try { DesktopUtils.browse(url.toURI()); } catch (Exception e) {}
                  }
              };
              classShareWarningArea.setOpaque(true);
              classShareWarningArea.setBackground(new java.awt.Color(255, 180, 180));
              classShareWarningArea.setForeground(new java.awt.Color(0, 0, 0));
              classShareWarningArea.setBorder(BorderFactory.createLineBorder(new java.awt.Color(180, 180, 180)));
              classShareWarningArea.setBorder(BorderFactory.createCompoundBorder(classShareWarningArea.getBorder(),
                      BorderFactory.createMatteBorder(5, 5, 5, 5, classShareWarningArea.getBackground())));
              classShareWarningArea.setVisible(classSharingBreaksProfiling); // NOI18N
              if (classSharingBreaksProfiling) {
                  String link;
                  if (DesktopUtils.isBrowseAvailable()) {
                      link = NbBundle.getMessage(ApplicationProfilerView.class, "MSG_Class_Sharing_Link");  // NOI18N
                  } else {
                      link = NbBundle.getMessage(ApplicationProfilerView.class, "MSG_Class_Sharing_Nolink");    // NOI18N
                  }
                  String message = NbBundle.getMessage(ApplicationProfilerView.class, "MSG_Class_Sharing", link);   // NOI18N
                  classShareWarningArea.setText(message);
              }
              constraints = new GridBagConstraints();
              constraints.gridx = 0;
              constraints.gridy = 1;
              constraints.gridwidth = GridBagConstraints.REMAINDER;
              constraints.fill = GridBagConstraints.HORIZONTAL;
              constraints.anchor = GridBagConstraints.WEST;
              constraints.insets = new Insets(4, 8, 10, 8);
              controlPanel.add(classShareWarningArea, constraints);

              // modeLabel
              modeLabel = new JLabel(NbBundle.getMessage(ApplicationProfilerView.class, "LBL_Profile"));    // NOI18N
              modeLabel.setFont(modeLabel.getFont().deriveFont(Font.BOLD));
              modeLabel.setOpaque(false);
              constraints = new GridBagConstraints();
              constraints.gridx = 0;
              constraints.gridy = 2;
              constraints.gridwidth = 1;
              constraints.fill = GridBagConstraints.NONE;
              constraints.anchor = GridBagConstraints.WEST;
              constraints.insets = new Insets(4, 8, 0, 0);
              controlPanel.add(modeLabel, constraints);

              // cpuButton
              cpuButton = new OneWayToggleButton(NbBundle.getMessage(ApplicationProfilerView.class, "LBL_Cpu"));    // NOI18N
              cpuButton.setIcon(new ImageIcon(Utilities.loadImage("com/sun/tools/visualvm/profiler/resources/cpu.png", true))); // NOI18N
              cpuButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { handleCPUProfiling(); }
              });
              constraints = new GridBagConstraints();
              constraints.gridx = 2;
              constraints.gridy = 2;
              constraints.gridwidth = 1;
              constraints.fill = GridBagConstraints.NONE;
              constraints.anchor = GridBagConstraints.WEST;
              constraints.insets = new Insets(4, 8, 0, 0);
              controlPanel.add(cpuButton, constraints);

              // memoryButton
              memoryButton = new OneWayToggleButton(NbBundle.getMessage(ApplicationProfilerView.class, "LBL_Memory"));  // NOI18N
              memoryButton.setIcon(new ImageIcon(Utilities.loadImage("com/sun/tools/visualvm/profiler/resources/memory.png", true)));   // NOI18N
              memoryButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { handleMemoryProfiling(); }
              });
              constraints = new GridBagConstraints();
              constraints.gridx = 3;
              constraints.gridy = 2;
              constraints.gridwidth = 1;
              constraints.fill = GridBagConstraints.NONE;
              constraints.anchor = GridBagConstraints.WEST;
              constraints.insets = new Insets(4, 8, 0, 0);
              controlPanel.add(memoryButton, constraints);

              // stopButton
              stopButton = new JButton(NbBundle.getMessage(ApplicationProfilerView.class, "LBL_Stop")); // NOI18N
              stopButton.setIcon(new ImageIcon(Utilities.loadImage("com/sun/tools/visualvm/profiler/resources/stop.png", true)));   // NOI18N
              stopButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { handleStopProfiling(); }
              });
              stopButton.setEnabled(false);
              constraints = new GridBagConstraints();
              constraints.gridx = 4;
              constraints.gridy = 2;
              constraints.gridwidth = 1;
              constraints.fill = GridBagConstraints.NONE;
              constraints.anchor = GridBagConstraints.WEST;
              constraints.insets = new Insets(4, 8, 0, 0);
              controlPanel.add(stopButton, constraints);

              // filler
              JPanel filler1 = new JPanel(new BorderLayout());
              filler1.setOpaque(false);
              constraints = new GridBagConstraints();
              constraints.gridx = 5;
              constraints.gridy = 2;
              constraints.weightx = 1;
              constraints.weighty = 1;
              constraints.gridwidth = GridBagConstraints.REMAINDER;
              constraints.fill = GridBagConstraints.BOTH;
              constraints.anchor = GridBagConstraints.NORTHWEST;
              constraints.insets = new Insets(0, 0, 0, 0);
              controlPanel.add(filler1, constraints);

              // statusLabel
              statusLabel = new JLabel(NbBundle.getMessage(ApplicationProfilerView.class, "LBL_Status"));   // NOI18N
              statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
              statusLabel.setOpaque(false);
              constraints = new GridBagConstraints();
              constraints.gridx = 0;
              constraints.gridy = 3;
              constraints.gridwidth = 1;
              constraints.fill = GridBagConstraints.NONE;
              constraints.anchor = GridBagConstraints.WEST;
              constraints.insets = new Insets(6, 8, 4, 0);
              controlPanel.add(statusLabel, constraints);

              // statusValueLabel
              statusValueLabel = new HTMLLabel() {
                public void setText(String text) {
                  super.setText("<nobr>" + text + "</nobr>");   // NOI18N
                }
                protected void showURL(URL url) {
                  ProfilerSupport.getInstance().selectActiveProfilerView();
                }

                // NOTE: overriding dimensions prevents UI "jumping" when changing the link
                public Dimension getPreferredSize() { return new Dimension(super.getPreferredSize().width, refLabelHeight); }
                public Dimension getMinimumSize() { return getPreferredSize(); }
                public Dimension getMaximumSize() { return getPreferredSize(); }
              };
              statusValueLabel.setOpaque(false);
              constraints = new GridBagConstraints();
              constraints.gridx = 1;
              constraints.gridy = 3;
              constraints.gridwidth = GridBagConstraints.REMAINDER;
              constraints.fill = GridBagConstraints.NONE;
              constraints.anchor = GridBagConstraints.WEST;
              constraints.insets = new Insets(6, 8, 4, 8);
              controlPanel.add(statusValueLabel, constraints);

              // filler
              JPanel filler2 = new JPanel(new BorderLayout());
              filler2.setOpaque(false);
              constraints = new GridBagConstraints();
              constraints.gridx = 2;
              constraints.gridy = 3;
              constraints.weightx = 1;
              constraints.weighty = 1;
              constraints.gridwidth = GridBagConstraints.REMAINDER;
              constraints.fill = GridBagConstraints.BOTH;
              constraints.anchor = GridBagConstraints.NORTHWEST;
              constraints.insets = new Insets(0, 0, 0, 0);
              controlPanel.add(filler2, constraints);
            
              Dimension cpuD     = cpuButton.getPreferredSize();
              Dimension memoryD  = memoryButton.getPreferredSize();
              Dimension stopD    = stopButton.getPreferredSize();

              Dimension maxD = new Dimension(Math.max(cpuD.width, memoryD.width), Math.max(cpuD.height, memoryD.height));
              maxD = new Dimension(Math.max(maxD.width, stopD.width), Math.max(maxD.height, stopD.height));

              cpuButton.setPreferredSize(maxD);
              cpuButton.setMinimumSize(maxD);
              memoryButton.setPreferredSize(maxD);
              memoryButton.setMinimumSize(maxD);
              stopButton.setPreferredSize(maxD);
              stopButton.setMinimumSize(maxD);
            
              setOpaque(true);
              setBackground(Color.WHITE);
              
              setLayout(new BorderLayout());
              setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
              add(controlPanel, BorderLayout.CENTER);
        }
        
        private HTMLTextArea classShareWarningArea;
        private JLabel modeLabel;
        private JToggleButton cpuButton;
        private JToggleButton memoryButton;
        private JButton stopButton;
        private JLabel statusLabel;
        private HTMLLabel statusValueLabel;
        private static final int refLabelHeight = new HTMLLabel("X").getPreferredSize().height; // NOI18N
        
    }
    
    private static final class OneWayToggleButton extends JToggleButton {
    
        public OneWayToggleButton(String text) {
          super(text);
        }

        protected void processMouseEvent(MouseEvent e) {
          if (!isSelected()) super.processMouseEvent(e);
        }

    }

}
