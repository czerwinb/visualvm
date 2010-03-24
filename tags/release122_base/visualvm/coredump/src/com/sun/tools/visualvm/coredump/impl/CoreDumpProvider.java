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

package com.sun.tools.visualvm.coredump.impl;

import com.sun.tools.visualvm.core.datasource.DataSourceRepository;
import com.sun.tools.visualvm.core.datasource.Storage;
import com.sun.tools.visualvm.core.snapshot.Snapshot;
import com.sun.tools.visualvm.core.datasupport.Utils;
import com.sun.tools.visualvm.core.explorer.ExplorerSupport;
import com.sun.tools.visualvm.core.datasource.descriptor.DataSourceDescriptor;
import com.sun.tools.visualvm.core.datasource.descriptor.DataSourceDescriptorFactory;
import com.sun.tools.visualvm.coredump.CoreDumpSupport;
import com.sun.tools.visualvm.coredump.CoreDumpsContainer;
import com.sun.tools.visualvm.tools.sa.SaModel;
import com.sun.tools.visualvm.tools.sa.SaModelFactory;
import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.lib.profiler.ui.SwingWorker;
import org.netbeans.modules.profiler.NetBeansProfiler;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

/**
 *
 * @author Tomas Hurka
 * @author Jiri Sedlacek
 */
public class CoreDumpProvider {
    private static final Logger LOGGER = Logger.getLogger(CoreDumpProvider.class.getName());
    
    private static final String SNAPSHOT_VERSION = "snapshot_version";  // NOI18N
    private static final String SNAPSHOT_VERSION_DIVIDER = "."; // NOI18N
    private static final String CURRENT_SNAPSHOT_VERSION_MAJOR = "1";   // NOI18N
    private static final String CURRENT_SNAPSHOT_VERSION_MINOR = "0";   // NOI18N
    private static final String CURRENT_SNAPSHOT_VERSION = CURRENT_SNAPSHOT_VERSION_MAJOR + SNAPSHOT_VERSION_DIVIDER + CURRENT_SNAPSHOT_VERSION_MINOR;
    
    private static final String PROPERTY_JAVA_HOME = "prop_java_home";  // NOI18N
    
    private static class CoreDumpAdder extends SwingWorker {
        volatile private ProgressHandle ph = null;
        volatile private boolean success = false;
        private CoreDumpImpl newCoreDump;
        private Storage storage;
        private String[] propNames, propValues;
        
        public CoreDumpAdder(CoreDumpImpl newCoreDump, Storage storage, String[] propNames, String[] propValues) {
            this.newCoreDump = newCoreDump;
            this.storage = storage;
            this.propValues = propValues;
            this.propNames = propNames;
        }
        
        @Override
        protected void doInBackground() {
            SaModel model = SaModelFactory.getSAAgentFor(newCoreDump);
            if (model != null) {
                storage.setCustomProperties(propNames, propValues);
                CoreDumpsContainer.sharedInstance().getRepository().addDataSource(newCoreDump);

                success = true;
            }
        }

        @Override
        protected void nonResponding() {
            ph = ProgressHandleFactory.createHandle(NbBundle.getMessage(CoreDumpProvider.class, "LBL_Inspecting_core_dump"));   // NOI18N
            ph.start();
        }

        @Override
        protected void done() {
            if (ph != null) {
                ph.finish();
            }
            if (!success) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(CoreDumpProvider.class, "MSG_not_valid_core_dump", newCoreDump.getFile().getAbsolutePath())));  // NOI18N
            }
        }
    }
    
    static void createCoreDump(final String coreDumpFile, final String displayName, final String jdkHome, final boolean deleteCoreDump) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                createCoreDumpImpl(coreDumpFile, displayName, jdkHome, deleteCoreDump);
            }
        });
    }
    
    private static void createCoreDumpImpl(String coreDumpFile, final String displayName, String jdkHome, boolean deleteCoreDump) {
        
        // TODO: check if the same coredump isn't already imported (can happen for moved coredumps)
        
        final CoreDumpImpl knownCoreDump = getCoreDumpByFile(new File(coreDumpFile));
        if (knownCoreDump != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ExplorerSupport.sharedInstance().selectDataSource(knownCoreDump);
                    NetBeansProfiler.getDefaultNB().displayWarning(NbBundle.getMessage(CoreDumpProvider.class, "MSG_Core_dump_already_added", new Object[] {displayName,DataSourceDescriptorFactory.getDescriptor(knownCoreDump).getName()}));  // NOI18N
                }
            });
            return;
        }
        
        if (deleteCoreDump) {
            ProgressHandle pHandle = null;
            try {
                pHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(CoreDumpProvider.class, "MSG_Adding", displayName));   // NOI18N
                pHandle.setInitialDelay(0);
                pHandle.start();
                
                File file = new File(coreDumpFile);
                File copy = Utils.getUniqueFile(CoreDumpSupport.getStorageDirectory(), file.getName());
                if (Utils.copyFile(file, copy)) {
                    coreDumpFile = copy.getAbsolutePath();
                    if (!file.delete()) file.deleteOnExit();
                }
            } finally {
                final ProgressHandle pHandleF = pHandle;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() { if (pHandleF != null) pHandleF.finish(); }
                });
            }
        }
        
        final String[] propNames = new String[] {
            SNAPSHOT_VERSION,
            Snapshot.PROPERTY_FILE,
            DataSourceDescriptor.PROPERTY_NAME,
            PROPERTY_JAVA_HOME };
        final String[] propValues = new String[] {
            CURRENT_SNAPSHOT_VERSION,
            coreDumpFile,
            displayName,
            jdkHome
        };

        File customPropertiesStorage = Utils.getUniqueFile(CoreDumpSupport.getStorageDirectory(), new File(coreDumpFile).getName(), Storage.DEFAULT_PROPERTIES_EXT);
        Storage storage = new Storage(customPropertiesStorage.getParentFile(), customPropertiesStorage.getName());
        
        try {
            CoreDumpImpl newCoreDump = new CoreDumpImpl(new File(coreDumpFile), new File(jdkHome), storage);
            if (newCoreDump != null) {
                new CoreDumpAdder(newCoreDump, storage, propNames, propValues).execute();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating coredump", e); // NOI18N
            return;
        }
    }
    
    private static CoreDumpImpl getCoreDumpByFile(File file) {
        if (!file.isFile()) return null;
        Set<CoreDumpImpl> knownCoredumps = DataSourceRepository.sharedInstance().getDataSources(CoreDumpImpl.class);
        for (CoreDumpImpl knownCoredump : knownCoredumps)
            if (knownCoredump.getFile().equals(file)) return knownCoredump;
        return null;
    }
    
    private void initPersistedCoreDumps() {
        if (!CoreDumpSupport.storageDirectoryExists()) return;
        
        File[] files = CoreDumpSupport.getStorageDirectory().listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(Storage.DEFAULT_PROPERTIES_EXT);
            }
        });
        
        Set<CoreDumpImpl> coredumps = new HashSet();
        for (File file : files) {
            Storage storage = new Storage(file.getParentFile(), file.getName());
            String[] propNames = new String[] {
                Snapshot.PROPERTY_FILE,
                PROPERTY_JAVA_HOME
            };
            String[] propValues = storage.getCustomProperties(propNames);
                
            CoreDumpImpl persistedCoredump = null;
            try {
                persistedCoredump = new CoreDumpImpl(new File(propValues[0]), new File(propValues[1]), storage);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error loading persisted host", e);    // NOI18N
            }
            
            if (persistedCoredump != null) coredumps.add(persistedCoredump);
        }
        
        if (!coredumps.isEmpty())
            CoreDumpsContainer.sharedInstance().getRepository().addDataSources(coredumps);
    }
    
    
    CoreDumpProvider() {
    }
    
    public static void register() {
        if (Utilities.isWindows()) return;
        final CoreDumpProvider provider = new CoreDumpProvider();
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            public void run() {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        provider.initPersistedCoreDumps();
                    }
                });
            }
        });
    }
  
}