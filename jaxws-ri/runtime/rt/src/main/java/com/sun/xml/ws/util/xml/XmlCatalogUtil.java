/*
 * Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
// Portions Copyright [2019] [Payara Foundation and/or its affiliates]

package com.sun.xml.ws.util.xml;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import javax.xml.ws.WebServiceException;

import org.xml.sax.EntityResolver;

import com.sun.istack.Nullable;
import com.sun.org.apache.xml.internal.resolver.Catalog;
import com.sun.org.apache.xml.internal.resolver.CatalogManager;
import com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver;
import com.sun.xml.ws.api.ResourceLoader;
import com.sun.xml.ws.server.ServerRtException;
import com.sun.xml.ws.util.ServiceFinder;

/**
 *
 * @author lukas
 */
public class XmlCatalogUtil {

    /**
     * Gets an EntityResolver using XML catalog
     * @param catalogUrl
     * @return
     */
    public static EntityResolver createEntityResolver(@Nullable URL catalogUrl) {
        // set up a manager
        CatalogManager manager = new CatalogManager();
        manager.setIgnoreMissingProperties(true);
        // Using static catalog may  result in to sharing of the catalog by multiple apps running in a container
        manager.setUseStaticCatalog(false);
        Catalog catalog = manager.getCatalog();
        try {
            if (catalogUrl != null) {
                catalog.parseCatalog(catalogUrl);
            }
        } catch (IOException e) {
            throw new ServerRtException("server.rt.err", e);
        }
        return workaroundCatalogResolver(catalog);
    }

    /**
     * Gets a default EntityResolver for catalog at META-INF/jaxws-catalog.xml
     * @return
     */
    public static EntityResolver createDefaultCatalogResolver() {

        // set up a manager
        CatalogManager manager = new CatalogManager();
        manager.setIgnoreMissingProperties(true);
        
        // Using static catalog may  result in to sharing of the catalog by multiple apps running in a container
        manager.setUseStaticCatalog(false);
        
        // Activates debug via system property, if set
        manager.getVerbosity();
        
        // Parse the catalog
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Catalog catalog = manager.getCatalog();
        parseResourcesToCatalog(cl, catalog, "META-INF/jax-ws-catalog.xml");
        parseResourcesToCatalog(catalog, "jax-ws-catalog.xml");

        return workaroundCatalogResolver(catalog);
    }
    
    private static void parseResourcesToCatalog(ClassLoader classLoader, Catalog catalog, String resourceName) {
        Enumeration<URL> catalogEnum;
        try {
            if (classLoader == null) {
                catalogEnum = ClassLoader.getSystemResources(resourceName);
            } else {
                catalogEnum = classLoader.getResources(resourceName);
            }

            while (catalogEnum.hasMoreElements()) {
                URL url = catalogEnum.nextElement();
                catalog.parseCatalog(url);
            }
        } catch (IOException e) {
            throw new WebServiceException(e);
        }
    }

    private static void parseResourcesToCatalog(Catalog catalog, String resourceName) {
        try {
            for (ResourceLoader resourceLoader : ServiceFinder.find(ResourceLoader.class)) {
                URL resource = resourceLoader.getResource(resourceName);
                if (resource != null) {
                    catalog.parseCatalog(resourceLoader.getResource(resourceName));
                }
            }
        } catch (IOException e) {
            throw new WebServiceException(e);
        }
    }

    /**
     * Default CatalogResolver implementation is broken as it depends on
     * CatalogManager.getCatalog() which will always create useStaticCatalog is
     * false. This returns a CatalogResolver that uses the catalog passed as
     * parameter.
     *
     * @param catalog
     * @return CatalogResolver
     */
    private static CatalogResolver workaroundCatalogResolver(final Catalog catalog) {
        // set up a manager
        CatalogManager manager = new CatalogManager() {
            @Override
            public Catalog getCatalog() {
                return catalog;
            }
        };
        manager.setIgnoreMissingProperties(true);
        // Using static catalog may  result in to sharing of the catalog by multiple apps running in a container
        manager.setUseStaticCatalog(false);

        return new CatalogResolver(manager);
    }

}
