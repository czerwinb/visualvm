/*
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
package net.java.visualvm.modules.glassfish;

import com.sun.tools.visualvm.core.datasource.Application;
import com.sun.tools.visualvm.core.model.jvm.JVM;
import java.awt.Image;
import java.text.MessageFormat;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;



/**
 *
 * @author Jaroslav Bachorik
 */
public class GlassFishInstanceType extends GlassFishApplicationType {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final String description = "(domain = {0})";
    private String domainName = "UNKNOWN";
    private final String typeName = "GlassFish/SJSAS (pid {0})";
    private JVM gfJvm;
    
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public GlassFishInstanceType(Application app, JVM jvm) {
        super(app.getPid());
        init(app, jvm);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    @Override
    public String getDescription() {
        return MessageFormat.format(description, domainName);
    }

    @Override
    public Image getIcon() {
        Image icon = super.getIcon();

        return Utilities.mergeImages(icon,
                                     Utilities.loadImage("net/java/visualvm/modules/glassfish/resources/instance_badge.png", true),
                                     8, 8);
    }

    @Override
    public String getName() {
        return MessageFormat.format(typeName, appPID);
    }

    @Override
    public String getVersion() {
        return "0";
    }

    private void init(Application app, JVM jvm) {
        try {
            if (jvm.isGetSystemPropertiesSupported()) {
              domainName = jvm.getSystemProperties().getProperty("com.sun.aas.domainName", domainName);
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}