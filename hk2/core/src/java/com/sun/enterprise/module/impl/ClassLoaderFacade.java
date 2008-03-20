/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.module.impl;

import com.sun.enterprise.module.impl.Utils;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.ArrayList;

/**
 * Facade for {@link ModuleClassLoader} to only expose public classes.
 *
 * @author dochez
 */
final class ClassLoaderFacade extends URLClassLoader {
 
    private final static URL[] EMPTY_URLS = new URL[0];
    private HashSet<String> publicPkgs = null;
    private ArrayList<String> publicSet = null;
    private ModuleClassLoader privateLoader;
    private int classesLoaded = 0;

    /** Creates a new instance of ClassLoaderFacade */
    public ClassLoaderFacade(ModuleClassLoader privateLoader) {
        super(EMPTY_URLS, privateLoader.getParent());
        this.privateLoader = privateLoader;
    }

    protected void finalize() throws Throwable {
        super.finalize();
        Utils.getDefaultLogger().fine("Facade ClassLoader killed " + privateLoader.getOwner().getModuleDefinition().getName());
        privateLoader.stop();
    }

    public void setPublicPkgs(String[] publicPkgs) {
        
        if (publicPkgs==null || publicPkgs.length==0)
            return;
        
        for (String publicPkg : publicPkgs) {
            if (publicPkg.endsWith(".")) {
                if (publicSet==null) {
                    publicSet = new ArrayList<String>();
                }
                publicSet.add(publicPkg);
            } else {
                if (this.publicPkgs==null) {
                    this.publicPkgs = new HashSet<String>();
                }
                this.publicPkgs.add(publicPkg);
            }
        }
    }
    
    public String[] getPublicPkgs() {
        return publicPkgs.toArray(new String[publicPkgs.size()]);
    }
    
    boolean matchExportedPackage(String name) {
        if (publicPkgs==null && publicSet==null) {
            return true;
        }
        if (publicSet!=null) {
            for (String aPublicSet : publicSet) {
                if (name.startsWith(aPublicSet)) {
                    return true;
                 }
            }
        }
        if (publicPkgs==null) {
            return false;
        }
        int index = name.lastIndexOf('.');
        if (index==-1) {
            return false;
        }
        String packageName = name.substring(0, index);
        return publicPkgs.contains(packageName);
    }
              
    protected Class findClass(String name) throws ClassNotFoundException {
        if (matchExportedPackage(name)) {
            return privateLoader.loadClass(name);
        } 
        throw new ClassNotFoundException(name);
    }
    
    public Enumeration<URL> findResources(String name) throws IOException {
        return privateLoader.findResources(name);
    }
    
    public URL findResource(String name) {
        return privateLoader.findResource(name);
    }  
    
/*    public Enumeration<URL> getResources(String name) throws IOException {
        
        return privateLoader.getResources(name);    
    }
    
    public URL getResource(String name) {
        
        return privateLoader.getResource(name);    
    }    
    
*/                
/*    public Class loadClass(String name) throws ClassNotFoundException {
        if (matchExportedPackage(name)) {
            if (!initialized) {
                initialize(name);
            }
            Class c = privateLoader.loadClass(name);
            if (c!=null) {
                classesLoaded++;
            } 
            return c;
                
        } 
        throw new ClassNotFoundException(name);        
    }
*/

    /**
     * Tries to find a class from the {@link ModuleClassLoader} that this facade is wrapping,
     * without doing further delegation to ancestors.
     */
    Class getClass(String name) throws ClassNotFoundException {
        if (matchExportedPackage(name)) {
            Class c = privateLoader.findClassDirect(name);
            classesLoaded++;
            return c;
        }
        return null;
        
    }

    /**
     * Works like {@link #findResource(String)} but only looks at
     * this module, without delegating to ancestors.
     */
    URL findResourceDirect(String name) {
        return privateLoader.findResourceDirect(name);
    }

    public void dumpState(PrintStream writer) {
        privateLoader.dumpState(writer);
        writer.println("Nb of classes loaded " + classesLoaded);
    }
    
    public String toString() {
        return super.toString() + " Facade for " + privateLoader.toString();
    }

}
