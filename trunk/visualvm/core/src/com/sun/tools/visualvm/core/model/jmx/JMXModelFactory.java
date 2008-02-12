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

package com.sun.tools.visualvm.core.model.jmx;

import com.sun.tools.visualvm.core.application.JmxApplication;
import com.sun.tools.visualvm.core.application.JvmstatApplication;
import com.sun.tools.visualvm.core.model.ModelFactory;
import com.sun.tools.visualvm.core.model.ModelProvider;
import com.sun.tools.visualvm.core.datasource.Application;

/**
 *
 * @author Luis-Miguel Alventosa
 */
public class JMXModelFactory
        extends ModelFactory<JMXModel, Application>
        implements ModelProvider<JMXModel, Application> {

    private static JMXModelFactory factory;

    private JMXModelFactory() {
    }

    public static synchronized JMXModelFactory getDefault() {
        if (factory == null) {
            factory = new JMXModelFactory();
            factory.registerFactory(factory);
        }
        return factory;
    }

    public static JMXModel getJmxModelFor(Application app) {
        return getDefault().getModel(app);
    }

    public JMXModel createModelFor(Application app) {
        if (app instanceof JvmstatApplication) {
            return new JMXModel((JvmstatApplication) app);
        } else if (app instanceof JmxApplication) {
            return new JMXModel((JmxApplication) app);
        }
        return null;
    }
}
