/*
 *  Copyright 2007-2008 Sun Microsystems, Inc.  All Rights Reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Sun designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Sun in the LICENSE file that accompanied this code.
 * 
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 * 
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 *  Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 *  CA 95054 USA or visit www.sun.com if you need additional information or
 *  have any questions.
 */
package com.sun.tools.visualvm.coredump.impl;

import com.sun.tools.visualvm.core.ui.actions.SingleDataSourceAction;
import com.sun.tools.visualvm.coredump.CoreDumpsContainer;
import java.awt.Image;
import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;
import org.openide.util.Utilities;

    
/**
 *
 * @author Jiri Sedlacek
 */
class AddVMCoredumpAction extends SingleDataSourceAction<CoreDumpsContainer> {
    
    private static final String ICON_PATH = "com/sun/tools/visualvm/coredump/resources/addCoredump.png";
    private static final Image ICON =  Utilities.loadImage(ICON_PATH);
    
    private boolean tracksSelection = false;
    
    private static AddVMCoredumpAction alwaysEnabled;
    private static AddVMCoredumpAction selectionAware;
    
    
    public static synchronized AddVMCoredumpAction alwaysEnabled() {
        if (alwaysEnabled == null) {
            alwaysEnabled = new AddVMCoredumpAction();
            alwaysEnabled.putValue(SMALL_ICON, new ImageIcon(ICON));
            alwaysEnabled.putValue("iconBase", ICON_PATH);
        }
        return alwaysEnabled;
    }
    
    public static synchronized AddVMCoredumpAction selectionAware() {
        if (selectionAware == null) {
            selectionAware = new AddVMCoredumpAction();
            selectionAware.tracksSelection = true;
        }
        return selectionAware;
    }
    
    public void actionPerformed(CoreDumpsContainer contanier, ActionEvent e) {
        CoreDumpConfigurator newCoreDumpConfiguration = CoreDumpConfigurator.defineCoreDump();
        if (newCoreDumpConfiguration != null) {
            CoreDumpProvider.createCoreDump(newCoreDumpConfiguration.getCoreDumpFile(),
                    newCoreDumpConfiguration.getDisplayname(), newCoreDumpConfiguration.getJavaHome(),
                    newCoreDumpConfiguration.deleteSourceFile());
        }
    }
    
    
    protected boolean isEnabled(CoreDumpsContainer contanier) {
        return true;
    }
    
    protected void initialize() {
        if (Utilities.isWindows()) setEnabled(false);
        else if (tracksSelection) super.initialize();
    }
    
    
    private AddVMCoredumpAction() {
        super(CoreDumpsContainer.class);
        putValue(NAME, "Add VM Coredump...");
        putValue(SHORT_DESCRIPTION, "Add VM Coredump");
    }
    
}