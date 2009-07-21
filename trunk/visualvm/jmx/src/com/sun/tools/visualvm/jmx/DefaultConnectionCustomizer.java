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

package com.sun.tools.visualvm.jmx;


import com.sun.tools.visualvm.core.datasource.DataSource;
import com.sun.tools.visualvm.core.explorer.ExplorerSupport;
import com.sun.tools.visualvm.host.Host;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.profiler.ui.stp.Utils;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import sun.net.util.IPAddressUtil;

/**
 * JmxConnectionCustomizer providing the default JMX Connection dialog to enter
 * JMX connection url and optional credentials.
 *
 * @since 1.2
 * @author Jiri Sedlacek
 */
public class DefaultConnectionCustomizer extends JmxConnectionCustomizer {

    DefaultConnectionCustomizer() {
        super(NbBundle.getMessage(DefaultConnectionCustomizer.class, "LBL_Default_jmx_connection_name"), // NOI18N
              NbBundle.getMessage(DefaultConnectionCustomizer.class, "LBL_Default_jmx_connection_descr"), // NOI18N
              1, false);
    }

    public DefaultPanel createPanel() {
        return new DefaultPanel();
    }

    public Setup getConnectionSetup(Panel customizerPanel) {
        if (!(customizerPanel instanceof DefaultPanel))
            throw new IllegalArgumentException("Panel must be DefaultPanel"); // NOI18N
        DefaultPanel panel = (DefaultPanel)customizerPanel;

        String connectionString = panel.getConnectionString();
        String displayName = panel.getDisplayName();
        EnvironmentProvider provider = JmxEnvironmentSupport.getInstance().
                createCredentialsProvider(panel.getUsername(), panel.getPassword(),
                                          panel.getSaveCredentials());
        boolean persistent = true;

        return new JmxConnectionCustomizer.Setup(connectionString, displayName,
                                                 provider, persistent);
    }


    /**
     * Returns true if the provided string represents a valid JMX url, false otherwise.
     *
     * @param txt JMX url to be checked
     * @return true if the provided string represents a valid JMX url, false otherwise
     */
    protected static boolean isValidConnectionString(String jmxurl) {
        boolean valid = false;
        if (jmxurl != null) {
            jmxurl = jmxurl.trim();
            if (jmxurl.startsWith("service:jmx:")) { // NOI18N
                if (jmxurl.length() > "service:jmx:".length()) { // NOI18N
                    valid = true;
                }
            } else {
                //---------------------------------------
                // Supported host and port combinations:
                //     hostname:port
                //     IPv4Address:port
                //     [IPv6Address]:port
                //---------------------------------------

                // Is literal IPv6 address?
                //
                if (jmxurl.startsWith("[")) { // NOI18N
                    int index = jmxurl.indexOf("]:"); // NOI18N
                    if (index != -1) {
                        // Extract literal IPv6 address
                        //
                        String address = jmxurl.substring(1, index);
                        if (IPAddressUtil.isIPv6LiteralAddress(address)) {
                            // Extract port
                            //
                            try {
                                String portStr = jmxurl.substring(index + 2);
                                int port = Integer.parseInt(portStr);
                                if (port >= 0 && port <= 0xFFFF) {
                                    valid = true;
                                }
                            } catch (NumberFormatException ex) {
                                valid = false;
                            }
                        }
                    }
                } else {
                    String[] s = jmxurl.split(":"); // NOI18N
                    if (s.length == 2) {
                        try {
                            int port = Integer.parseInt(s[1]);
                            if (port >= 0 && port <= 0xFFFF) {
                                valid = true;
                            }
                        } catch (NumberFormatException ex) {
                            valid = false;
                        }
                    }
                }
            }
        }
        return valid;
    }


    /**
     * Implementation of JmxConnectionCustomizer.Panel for entering the JMX url,
     * optional credentials and selecting whether the credentials should be
     * persistent or transient.
     *
     * @since 1.2
     * @author Jiri Sedlacek
     */
    public static class DefaultPanel extends Panel {

        /**
         * Creates new instance of DefaultPanel.
         */
        public DefaultPanel() {
            initComponents();
            initDefaults();
            update();
        }

        /**
         * Returns the JMX url defined by the panel.
         *
         * @return JMX url defined by the panel
         */
        public final String getConnectionString() {
            return connectionField.getText().trim();
        }

        /**
         * Returns connection name defined by the panel.
         *
         * @return connection name defined by the panel
         */
        public final String getDisplayName() {
            return displaynameField.getText().trim();
        }

        /**
         * Returns username defined by the panel.
         *
         * @return username defined by the panel
         */
        public final String getUsername() {
            return usernameField.getText().trim();
        }

        /**
         * Returns password defined by the panel.
         *
         * @return password defined by the panel
         */
        public final char[] getPassword() {
            return passwordField.getPassword();
        }

        /**
         * Returns true if the panel request to persist username and password,
         * false otherwise
         *
         * @return true if the panel request to persist username and password, false otherwise
         */
        public final boolean getSaveCredentials() {
            return saveCheckbox.isSelected();
        }


        private void initDefaults() {
            Set<DataSource> selectedDataSources =
                ExplorerSupport.sharedInstance().getSelectedDataSources();
            if (selectedDataSources.size() != 1) return;
            DataSource selectedDataSource = selectedDataSources.iterator().next();
            if (!(selectedDataSource instanceof Host)) return;
            Host host = (Host)selectedDataSource;
            connectionField.setText(host.getHostName() + ":"); // NOI18N
        }

        private void update() {
            if (internalChange) return;
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    String username = getUsername();
                    String url = getConnectionString();

                    if (!displaynameCheckbox.isSelected()) {
                        internalChange = true;
                        displaynameField.setText(
                                (username.isEmpty() ? "" : username + "@") + url); // NOI18N
                        internalChange = false;
                    }

                    String displayname = getDisplayName();
                    displaynameField.setEnabled(displaynameCheckbox.isSelected());

                    usernameField.setEnabled(securityCheckbox.isSelected());
                    passwordField.setEnabled(securityCheckbox.isSelected());
                    saveCheckbox.setEnabled(securityCheckbox.isSelected());

                    setSettingsValid(isValidConnectionString(url) &&
                            displayname.length() > 0);
                }
            });
        }
        
        private void initComponents() {
            setLayout(new GridBagLayout());
            GridBagConstraints constraints;

            // connectionLabel
            connectionLabel = new JLabel();
            Mnemonics.setLocalizedText(connectionLabel, NbBundle.getMessage(DefaultConnectionCustomizer.class, "LBL_Connection")); // NOI18N
            constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.gridwidth = 1;
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.EAST;
            constraints.insets = new Insets(0, 0, 0, 0);
            add(connectionLabel, constraints);

            // connectionField
            connectionField = new JTextField();
            connectionLabel.setLabelFor(connectionField);
            connectionField.setPreferredSize(
                    new Dimension(250, connectionField.getPreferredSize().height));
            connectionField.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    update();
                }
                public void removeUpdate(DocumentEvent e) {
                    update();
                }
                public void changedUpdate(DocumentEvent e) {
                    update();
                }
            });
            constraints = new GridBagConstraints();
            constraints.gridx = 1;
            constraints.gridy = 0;
            constraints.gridwidth = GridBagConstraints.REMAINDER;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = new Insets(0, 5, 0, 0);
            add(connectionField, constraints);

            // usageLabel
            Font normalLabelFont = connectionLabel.getFont();
            Font smallLabelFont =
                    normalLabelFont.deriveFont(normalLabelFont.getSize2D() - 1);
            usageLabel = new JLabel(NbBundle.getMessage(DefaultConnectionCustomizer.class, "LBL_Usage"));    // NOI18N
            usageLabel.setFont(smallLabelFont);
            constraints = new GridBagConstraints();
            constraints.gridx = 1;
            constraints.gridy = 1;
            constraints.gridwidth = GridBagConstraints.REMAINDER;
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = new Insets(0, 5, 0, 0);
            add(usageLabel, constraints);

            // displaynameCheckbox
            displaynameCheckbox = new JCheckBox();
            Mnemonics.setLocalizedText(displaynameCheckbox, NbBundle.getMessage(DefaultConnectionCustomizer.class, "LBL_Display_name")); // NOI18N
            displaynameCheckbox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    update();
                };
            });
            constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = 2;
            constraints.gridwidth = 1;
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.EAST;
            constraints.insets = new Insets(8, 4, 0, 0);
            add(displaynameCheckbox, constraints);

            // displaynameField
            displaynameField = new JTextField();
            displaynameField.setPreferredSize(
                    new Dimension(250, displaynameField.getPreferredSize().height));
            displaynameField.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    update();
                }
                public void removeUpdate(DocumentEvent e) {
                    update();
                }
                public void changedUpdate(DocumentEvent e) {
                    update();
                }
            });
            constraints = new GridBagConstraints();
            constraints.gridx = 1;
            constraints.gridy = 2;
            constraints.gridwidth = GridBagConstraints.REMAINDER;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = new Insets(8, 5, 0, 0);
            add(displaynameField, constraints);

            // securityCheckbox
            securityCheckbox = new JCheckBox();
            Mnemonics.setLocalizedText(securityCheckbox, NbBundle.getMessage(DefaultConnectionCustomizer.class, "LBL_Use_security_credentials")); // NOI18N
            securityCheckbox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    update();
                };
            });
            constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = 3;
            constraints.gridwidth = GridBagConstraints.REMAINDER;
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = new Insets(15, 0, 0, 0);
            add(securityCheckbox, constraints);

            // usernameLabel
            usernameLabel = new JLabel();
            Mnemonics.setLocalizedText(usernameLabel, NbBundle.getMessage(DefaultConnectionCustomizer.class, "LBL_Username")); // NOI18N
            constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = 4;
            constraints.gridwidth = 1;
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.EAST;
            constraints.insets = new Insets(5, 0, 0, 0);
            add(usernameLabel, constraints);

            // usernameField
            usernameField = new JTextField();
            usernameLabel.setLabelFor(usernameField);
            usernameField.setPreferredSize(
                    new Dimension(320, usernameField.getPreferredSize().height));
            usernameField.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    update();
                }
                public void removeUpdate(DocumentEvent e) {
                    update();
                }
                public void changedUpdate(DocumentEvent e) {
                    update();
                }
            });
            constraints = new GridBagConstraints();
            constraints.gridx = 1;
            constraints.gridy = 4;
            constraints.gridwidth = GridBagConstraints.REMAINDER;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = new Insets(5, 5, 0, 0);
            add(usernameField, constraints);

            // passwordLabel
            passwordLabel = new JLabel();
            Mnemonics.setLocalizedText(passwordLabel, NbBundle.getMessage(DefaultConnectionCustomizer.class, "LBL_Password")); // NOI18N
            constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = 5;
            constraints.gridwidth = 1;
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.EAST;
            constraints.insets = new Insets(8, 0, 0, 0);
            add(passwordLabel, constraints);

            // passwordField
            passwordField = new JPasswordField();
            passwordLabel.setLabelFor(passwordField);
            passwordField.setPreferredSize(
                    new Dimension(200, passwordField.getPreferredSize().height));
            passwordField.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    update();
                }
                public void removeUpdate(DocumentEvent e) {
                    update();
                }
                public void changedUpdate(DocumentEvent e) {
                    update();
                }
            });
            constraints = new GridBagConstraints();
            constraints.gridx = 1;
            constraints.gridy = 5;
            constraints.gridwidth = GridBagConstraints.REMAINDER;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = new Insets(8, 5, 0, 0);
            add(passwordField, constraints);

            // saveCheckbox
            saveCheckbox = new JCheckBox();   // NOI18N
            Mnemonics.setLocalizedText(saveCheckbox, NbBundle.getMessage(DefaultConnectionCustomizer.class, "LBL_Save_security_credentials")); // NOI18N
            saveCheckbox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    update();
                };
            });
            constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = 6;
            constraints.gridwidth = GridBagConstraints.REMAINDER;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.EAST;
            constraints.insets = new Insets(8, 30, 0, 0);
            add(saveCheckbox, constraints);

            // spacer
            JPanel spacer = Utils.createFillerPanel();
            constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = 7;
            constraints.weightx = 1;
            constraints.weighty = 1;
            constraints.gridwidth = GridBagConstraints.REMAINDER;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.insets = new Insets(10, 0, 0, 0);
            add(spacer, constraints);
            
            // UI tweaks
            displaynameCheckbox.setBorder(connectionLabel.getBorder());
        }


        private boolean internalChange = false;

        private JLabel connectionLabel;
        private JTextField connectionField;
        private JLabel usageLabel;
        private JCheckBox displaynameCheckbox;
        private JTextField displaynameField;
        private JCheckBox securityCheckbox;
        private JLabel usernameLabel;
        private JTextField usernameField;
        private JLabel passwordLabel;
        private JPasswordField passwordField;
        private JCheckBox saveCheckbox;
    }

}
