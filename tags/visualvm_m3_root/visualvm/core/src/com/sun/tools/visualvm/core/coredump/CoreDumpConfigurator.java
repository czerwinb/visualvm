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

package com.sun.tools.visualvm.core.coredump;

import com.sun.tools.visualvm.core.datasource.CoreDump;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.profiler.ui.ProfilerDialogs;
import org.netbeans.modules.profiler.ui.stp.Utils;
import org.openide.DialogDescriptor;

/**
 *
 * @author Jiri Sedlacek
 * @author Tomas Hurka
 * 
 */
class CoreDumpConfigurator extends JPanel {

  private boolean internalChange = false;

  public static CoreDumpConfigurator defineCoreDump() {
    CoreDumpConfigurator hc = getDefault();
    hc.setupDefineCoreDump();
    
    final DialogDescriptor dd = new DialogDescriptor(hc, "Add VM Coredump", true, new Object[] {
      hc.okButton, DialogDescriptor.CANCEL_OPTION }, hc.okButton, 0, null, null);
    final Dialog d = ProfilerDialogs.createDialog(dd);
    d.pack();
    d.setVisible(true);
    
    if (dd.getValue() == hc.okButton) return hc;
    else return null;
  }
  
  public static CoreDumpConfigurator renameCoreDump(CoreDump coreDump) {
    CoreDumpConfigurator hc = getDefault();
    hc.setupRenameCoreDump(coreDump);
    
    final DialogDescriptor dd = new DialogDescriptor(hc, "Rename VM Coredump", true, new Object[] {
      hc.okButton, DialogDescriptor.CANCEL_OPTION }, hc.okButton, 0, null, null);
    final Dialog d = ProfilerDialogs.createDialog(dd);
    d.pack();
    d.setVisible(true);
    
    if (dd.getValue() == hc.okButton) return hc;
    else return null;
  }
  
  public String getCoreDumpFile() {
    return coreDumpFileField.getText().trim();
  }
  
  public String getDisplayname() {
    return displaynameField.getText().trim();
  }
  
  public String getJavaHome() {
    return javaHomeFileField.getText().trim();
  }
  
  private static CoreDumpConfigurator defaultInstance;
  
  private CoreDumpConfigurator() {
    initComponents();
    update();
  }
  
  private static CoreDumpConfigurator getDefault() {
    if (defaultInstance == null) defaultInstance = new CoreDumpConfigurator();
    return defaultInstance;
  }
  
  private void setupDefineCoreDump() {
    coreDumpFileField.setEnabled(true);
    displaynameCheckbox.setSelected(false);
    displaynameCheckbox.setEnabled(true);
    coreDumpFileField.setText("");
    displaynameField.setText("");
    javaHomeFileField.setText(getCurrentJDKHome());
    javaHomeFileField.setEnabled(true);
  }
  
  private String getCurrentJDKHome() {
      String javaHome = System.getProperty("java.home");
      String jreSuffix = File.separator + "jre";
      if (javaHome.endsWith(jreSuffix)) javaHome = javaHome.substring(0, javaHome.length() - jreSuffix.length());
      return javaHome;
  }
  
  private void setupRenameCoreDump(CoreDump coreDump) {
    coreDumpFileField.setEnabled(false);
    displaynameCheckbox.setSelected(true);
    displaynameCheckbox.setEnabled(false);
    javaHomeFileField.setEnabled(false);
    coreDumpFileField.setText(coreDump.getFile().getAbsolutePath());
    displaynameField.setText(coreDump.getDisplayName());
    displaynameField.requestFocusInWindow();
    displaynameField.selectAll();
  }
  
  private void update() {
    if (internalChange) return;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        String coreDumpname = getCoreDumpFile();
        File coreDumpFile = new File(coreDumpname);
        
        String jdkHome = getJavaHome();
        File jdkHomeFile = new File(jdkHome);
        
        if (!displaynameCheckbox.isSelected()) {
          internalChange = true;
          displaynameField.setText(coreDumpname);
          internalChange = false;
        }
        
        String displayname = getDisplayname();
        displaynameField.setEnabled(displaynameCheckbox.isSelected());
        
        okButton.setEnabled(coreDumpFile.exists() && coreDumpFile.isFile() &&
                jdkHomeFile.exists() && jdkHomeFile.isDirectory() && displayname.length() > 0);
      }
    });
  }
  
  private void chooseJavaHome() {
      JFileChooser chooser = new JFileChooser(new File(getJavaHome()));
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      int returnVal = chooser.showOpenDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
          javaHomeFileField.setText(chooser.getSelectedFile().getAbsolutePath());
      }
  }

  private void chooseCoreDump() {
      JFileChooser chooser = new JFileChooser(new File(getCoreDumpFile()));
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      int returnVal = chooser.showOpenDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
          coreDumpFileField.setText(chooser.getSelectedFile().getAbsolutePath());
      }
  }
  
  private void initComponents() {
    setLayout(new GridBagLayout());
    GridBagConstraints constraints;
    
    // coreDumpFileLabel
    coreDumpFileLabel = new JLabel("VM Coredump file:");
    constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.gridwidth = 1;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.insets = new Insets(15, 10, 0, 0);
    add(coreDumpFileLabel, constraints);
    
    // coreDumpFileField
    coreDumpFileField = new JTextField();
    coreDumpFileField.setPreferredSize(new Dimension(250, coreDumpFileField.getPreferredSize().height));
    coreDumpFileField.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e)  { update(); }
      public void removeUpdate(DocumentEvent e)  { update(); }
      public void changedUpdate(DocumentEvent e) { update(); }
    });
    constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 0;
    constraints.gridwidth = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.insets = new Insets(15, 5, 0, 0);
    add(coreDumpFileField, constraints);
    
    // coreDumpFileButton
    coreDumpFileButton = new JButton("Browse...");
    coreDumpFileButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            chooseCoreDump();
        }
    });
    constraints = new GridBagConstraints();
    constraints.gridx = 2;
    constraints.gridy = 0;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.insets = new Insets(15, 5, 0, 10);
    add(coreDumpFileButton, constraints);    
    
    // javaHomeFileLabel
    javaHomeFileLabel = new JLabel("JDK home:");
    constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = 1;
    constraints.gridwidth = 1;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.insets = new Insets(8, 10, 0, 0);
    add(javaHomeFileLabel, constraints);
    
    // javaHomeFileField
    javaHomeFileField = new JTextField();
    javaHomeFileField.setPreferredSize(new Dimension(250, javaHomeFileField.getPreferredSize().height));
    javaHomeFileField.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e)  { update(); }
      public void removeUpdate(DocumentEvent e)  { update(); }
      public void changedUpdate(DocumentEvent e) { update(); }
    });
    constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.gridwidth = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.insets = new Insets(8, 5, 0, 0);
    add(javaHomeFileField, constraints);
    
    // javaHomeFileButton
    javaHomeFileButton = new JButton("Browse...");
    javaHomeFileButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            chooseJavaHome();
        }
    });
    constraints = new GridBagConstraints();
    constraints.gridx = 2;
    constraints.gridy = 1;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.insets = new Insets(8, 5, 0, 10);
    add(javaHomeFileButton, constraints);        
    
    // displaynameCheckbox
    displaynameCheckbox = new JCheckBox("Display name:");
    displaynameCheckbox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { update(); };
    });
    constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = 2;
    constraints.gridwidth = 1;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.insets = new Insets(8, 10, 0, 0);
    add(displaynameCheckbox, constraints);
    
    // displaynameField
    displaynameField = new JTextField();
    displaynameField.setPreferredSize(new Dimension(250, displaynameField.getPreferredSize().height));
    displaynameField.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e)  { update(); }
      public void removeUpdate(DocumentEvent e)  { update(); }
      public void changedUpdate(DocumentEvent e) { update(); }
    });
    constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 2;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.insets = new Insets(8, 5, 0, 10);
    add(displaynameField, constraints);
    
    // spacer
    JPanel spacer = Utils.createFillerPanel();
    constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = 3;
    constraints.weightx = 1;
    constraints.weighty = 1;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.NORTHWEST;
    constraints.insets = new Insets(0, 0, 15, 0);
    add(spacer, constraints);
    
    // okButton
    okButton = new JButton("OK");
    
    // UI tweaks
    displaynameCheckbox.setBorder(coreDumpFileLabel.getBorder());
  }
  
  private JLabel coreDumpFileLabel;
  private JTextField coreDumpFileField;
  private JButton coreDumpFileButton;
  private JLabel javaHomeFileLabel;
  private JTextField javaHomeFileField;
  private JButton javaHomeFileButton;
  private JCheckBox displaynameCheckbox;
  private JTextField displaynameField;
  
  private JButton okButton;
  
}