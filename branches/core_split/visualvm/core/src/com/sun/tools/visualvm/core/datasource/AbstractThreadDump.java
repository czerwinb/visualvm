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

package com.sun.tools.visualvm.core.datasource;

import com.sun.tools.visualvm.core.snapshot.SnapshotsSupport;
import com.sun.tools.visualvm.core.threaddump.ThreadDumpSupport;
import java.io.File;

/**
 * Abstract implementation of ThreadDump.
 *
 * @author Jiri Sedlacek
 */
public abstract class AbstractThreadDump extends AbstractSnapshot implements ThreadDump {
    
    /**
     * Creates new instance of AbstractThreadDump with the data stored in a file.
     * 
     * @param file file where thread dump is saved.
     */
    public AbstractThreadDump(File file) {
        this(file, null);
    }
    
    /**
     * Creates new instance of AbstractThreadDump with the data stored in a file and defined master.
     * 
     * @param file file where thread dump is saved,
     * @param master DataSource in whose window the thread dump will be displayed.
     */
    public AbstractThreadDump(File file, DataSource master) {
        super(file, ThreadDumpSupport.getInstance().getCategory(), master);
    }
    
    public boolean supportsSaveAs() {
        return true;
    }
    
    public void saveAs() {
        SnapshotsSupport.getInstance().saveAs(this, "Save Thread Dump As");
    }

}
