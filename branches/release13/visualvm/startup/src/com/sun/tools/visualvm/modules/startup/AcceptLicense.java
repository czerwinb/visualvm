/*
 * Copyright 2007-2010 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.tools.visualvm.modules.startup;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.openide.util.NbBundle;

/**
 * Displays LicensePanel to user. User must accept license to continue. 
 * if user does not accept license UserCancelException is thrown.
 *
 * @author  Marek Slama
 * @author Jiri Sedlacek
 */
public final class AcceptLicense {

    private static final String YES_AC = "yes"; // NOI18N
    private static final String NO_AC  = "no" ; // NOI18N
    
    private static JDialog d;
    private static String command;
    
    /**
     * If License was not accepted during installation user must accept it here.
     */
    public static void showLicensePanel() throws Exception {
        Utils.setSystemLaF();

        // Make sure the code following this call runs on JDK 6
        if (!VisualVMStartup.checkEnv())
            throw new org.openide.util.UserCancelException();

        URL url = AcceptLicense.class.getResource("LICENSE.txt"); // NOI18N
        LicensePanel licensePanel = new LicensePanel(url);
        ResourceBundle bundle = NbBundle.getBundle(AcceptLicense.class);
        String yesLabel = bundle.getString("MSG_LicenseYesButton"); // NOI18N
        String noLabel = bundle.getString("MSG_LicenseNoButton"); // NOI18N
        JButton yesButton = new JButton();
        JButton noButton = new JButton();
        Utils.setLocalizedText(yesButton, yesLabel);
        Utils.setLocalizedText(noButton, noLabel);
        ActionListener listener = new ActionListener () {
            public void actionPerformed(ActionEvent e) {
                command = e.getActionCommand();
                d.setVisible(false);
                d.dispose();
                d = null;
            }            
        };
        yesButton.addActionListener(listener);
        noButton.addActionListener(listener);
        
        yesButton.setActionCommand(YES_AC);
        noButton.setActionCommand(NO_AC);
        
        yesButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_AcceptButton")); // NOI18N
        yesButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSD_AcceptButton")); // NOI18N
        
        noButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_RejectButton")); // NOI18N
        noButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSD_RejectButton")); // NOI18N
        
        Dimension yesPF = yesButton.getPreferredSize();
        Dimension noPF = noButton.getPreferredSize();
        int maxWidth = Math.max(yesPF.width, noPF.width);
        int maxHeight = Math.max(yesPF.height, noPF.height);
        yesButton.setPreferredSize(new Dimension(maxWidth, maxHeight));
        noButton.setPreferredSize(new Dimension(maxWidth, maxHeight));

        // Bugfix #361, set the JDialog to appear in the Taskbar on Windows
        d = new JDialog(null, bundle.getString("MSG_LicenseDlgTitle"), // NOI18N
                        JDialog.ModalityType.APPLICATION_MODAL);

        // Bugfix #361, JDialog should use the VisualVM icon for better identification
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        List<Image> icons = new ArrayList();
        icons.add(toolkit.createImage(AcceptLicense.class.getResource(
                "/com/sun/tools/visualvm/modules/startup/resources/icon16.png"))); // NOI18N
        icons.add(toolkit.createImage(AcceptLicense.class.getResource(
                "/com/sun/tools/visualvm/modules/startup/resources/icon24.png"))); // NOI18N
        icons.add(toolkit.createImage(AcceptLicense.class.getResource(
                "/com/sun/tools/visualvm/modules/startup/resources/icon32.png"))); // NOI18N
        icons.add(toolkit.createImage(AcceptLicense.class.getResource(
                "/com/sun/tools/visualvm/modules/startup/resources/icon48.png"))); // NOI18N
        d.setIconImages(icons);
        
        d.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_LicenseDlg")); // NOI18N
        d.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_LicenseDlg")); // NOI18N
        
        d.getContentPane().add(licensePanel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(17, 12, 11, 11));
        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);
        d.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        d.setSize(new Dimension(600, 600));
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        d.setResizable(true);
        d.setLocationRelativeTo(null);
        
        // Bugfix #361, ensure that the dialog will be the topmost visible window after opening
        Utils.makeAssertive(d);
        
        d.setVisible(true);
        
        if (YES_AC.equals(command)) {
            return;
        } else {
            throw new org.openide.util.UserCancelException();
        }
    }
    
}
