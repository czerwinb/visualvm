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

import com.sun.tools.visualvm.core.datasource.AbstractCoreDump;
import com.sun.tools.visualvm.core.datasupport.Storage;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Tomas Hurka
 * @author Jiri Sedlacek
 */
class CoreDumpImpl extends AbstractCoreDump {
    
    static final String PROPERTY_JAVA_HOME = "prop_java_home";
    
    private Storage storage;
    
    
    public CoreDumpImpl(Storage storage) throws IOException {
        super(new File(storage.getCustomProperty(PROPERTY_FILE)),
                new File(storage.getCustomProperty(PROPERTY_JAVA_HOME)));
        this.storage = storage;
    }
    
    public boolean supportsDelete() {
        return CoreDumpSupport.getStorageDirectory().equals(getFile().getParentFile());
    }
    
    // removes link to coredump on the filesystem
    void remove() {
        getStorage().deleteCustomPropertiesStorage();
        CoreDumpSupport.getProvider().unregisterCoreDump(this);
    }
    
    // deletes coredump stored in VisualVM persistent storage
    public void delete() {
        super.delete();
        CoreDumpSupport.getProvider().unregisterCoreDump(this);
    }
    
    
    protected Storage createStorage() {
        return storage;
    }
    
    void finished() {
        setState(STATE_FINISHED); 
    }
    
}
