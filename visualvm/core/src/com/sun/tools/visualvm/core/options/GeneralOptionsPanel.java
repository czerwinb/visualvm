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

package com.sun.tools.visualvm.core.options;

import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

final class GeneralOptionsPanel extends javax.swing.JPanel {

    final private static Logger LOGGER = Logger.getLogger("com.sun.tools.visualvm.core.options");
    private final GeneralOptionsPanelController controller;

    private final ChangeListener changeListener = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            controller.changed();
        }
    };
    
    GeneralOptionsPanel(GeneralOptionsPanelController controller) {
        this.controller = controller;
        initComponents();
        startTrackingChanges();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        mhRefresh = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        thrdRefresh = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        dataRefresh = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        instrFilter = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();

        jLabel1.setDisplayedMnemonic('h');
        jLabel1.setLabelFor(mhRefresh);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "Monitored Host:");

        jLabel2.setDisplayedMnemonic('t');
        jLabel2.setLabelFor(thrdRefresh);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, "Threads:");

        jLabel3.setDisplayedMnemonic('m');
        jLabel3.setLabelFor(dataRefresh);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, "Monitored Data:");

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, "Polling");

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, "Profiler");

        instrFilter.setMnemonic('p');
        org.openide.awt.Mnemonics.setLocalizedText(instrFilter, "Profile Java Core Classes");

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, "sec.");

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, "sec.");

        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, "sec.");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(instrFilter)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel4)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                                    .add(layout.createSequentialGroup()
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(jLabel1)
                                            .add(jLabel2)
                                            .add(jLabel3))
                                        .add(35, 35, 35)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                            .add(dataRefresh)
                                            .add(org.jdesktop.layout.GroupLayout.TRAILING, thrdRefresh)
                                            .add(org.jdesktop.layout.GroupLayout.TRAILING, mhRefresh, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(jLabel6)
                                            .add(jLabel8)
                                            .add(jLabel7))))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(jLabel5)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel4)
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jLabel6)
                    .add(mhRefresh, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(jLabel7)
                    .add(thrdRefresh, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(jLabel8)
                    .add(dataRefresh, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel5)
                    .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(instrFilter)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    void load() {
        // TODO read settings and initialize GUI
        // Example:        
        // someCheckBox.setSelected(Preferences.userNodeForPackage(CorePanel.class).getBoolean("someFlag", false));
        // or for org.openide.util with API spec. version >= 7.4:
        // someCheckBox.setSelected(NbPreferences.forModule(CorePanel.class).getBoolean("someFlag", false));
        // or:
        // someTextField.setText(SomeSystemOption.getDefault().getSomeStringProperty());
        mhRefresh.setValue(GlobalPreferences.sharedInstance().getMonitoredHostPoll());
        dataRefresh.setValue(GlobalPreferences.sharedInstance().getMonitoredDataPoll());
        thrdRefresh.setValue(GlobalPreferences.sharedInstance().getThreadsPoll());
        instrFilter.setSelected(GlobalPreferences.sharedInstance().isProfilerInstrFilter());
    }

    void store() {
        GlobalPreferences.sharedInstance().setMonitoredHostPoll((Integer) mhRefresh.getValue());
        GlobalPreferences.sharedInstance().setMonitoredDataPoll((Integer) dataRefresh.getValue());
        GlobalPreferences.sharedInstance().setThreadsPoll((Integer) thrdRefresh.getValue());
        GlobalPreferences.sharedInstance().setProfilerInstrFilter(instrFilter.isSelected());
        // TODO store modified settings
        // Example:
        // Preferences.userNodeForPackage(CorePanel.class).putBoolean("someFlag", someCheckBox.isSelected());
        // or for org.openide.util with API spec. version >= 7.4:
        // NbPreferences.forModule(CorePanel.class).putBoolean("someFlag", someCheckBox.isSelected());
        // or:
        // SomeSystemOption.getDefault().setSomeStringProperty(someTextField.getText());
        GlobalPreferences.sharedInstance().store();
    }

    boolean valid() {
        try {
            int mh = (Integer) mhRefresh.getValue();
            int md = (Integer) dataRefresh.getValue();
            int th = (Integer) thrdRefresh.getValue();
            return mh > 0 && md > 0 && th > 0;
        } catch (Exception e) {
        }
        return false;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner dataRefresh;
    private javax.swing.JCheckBox instrFilter;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSpinner mhRefresh;
    private javax.swing.JSpinner thrdRefresh;
    // End of variables declaration//GEN-END:variables

    private void startTrackingChanges() {
        mhRefresh.getModel().addChangeListener(changeListener);
        thrdRefresh.getModel().addChangeListener(changeListener);
        dataRefresh.getModel().addChangeListener(changeListener);
    }
}
