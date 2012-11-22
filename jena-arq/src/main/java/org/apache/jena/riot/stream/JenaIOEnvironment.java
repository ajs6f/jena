/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.riot.stream;

import java.util.StringTokenizer ;

import org.apache.jena.riot.TypedInputStream2 ;
import org.apache.jena.riot.adapters.AdapterFileManager ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.JenaRuntime ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.util.FileUtils ;
import com.hp.hpl.jena.vocabulary.LocationMappingVocab ;

/** Code for using the general facilities of the location mapper/ filemanager subsystem
 *  and set up for Jena usage. e.g. find a location mapper with RDf description. 
 */
class JenaIOEnvironment
{
    static LocationMapper2 theMapper = null ;
    /** Get the global LocationMapper */
    public static LocationMapper2 getLocationMapper()
    {
        if ( theMapper == null )
        {
            theMapper = new LocationMapper2() ;
            if ( getGlobalConfigPath() != null )
                JenaIOEnvironment.createLocationMapper(getGlobalConfigPath()) ;
        }
        return theMapper ;
    }
    
    static Logger log = LoggerFactory.getLogger(JenaIOEnvironment.class)  ;
    
    /** The default path for searching for the location mapper */
    public static final String DEFAULT_PATH =
        "file:location-mapping.rdf;file:location-mapping.n3;file:location-mapping.ttl;"+
        "file:etc/location-mapping.rdf;file:etc/location-mapping.n3;"+
        "file:etc/location-mapping.ttl" ;
    public static final String GlobalMapperSystemProperty1 = "http://jena.hpl.hp.com/2004/08/LocationMap" ;
    public static final String GlobalMapperSystemProperty2 = "LocationMap" ;

    static String s_globalMapperPath = null ; 

    static private String getGlobalConfigPath()
    {
        if ( s_globalMapperPath == null )
            s_globalMapperPath = JenaRuntime.getSystemProperty(GlobalMapperSystemProperty1,null) ;
        if ( s_globalMapperPath == null )
            s_globalMapperPath = JenaRuntime.getSystemProperty(GlobalMapperSystemProperty2,null) ;
        if ( s_globalMapperPath == null )
            s_globalMapperPath = DEFAULT_PATH ;
        return s_globalMapperPath ;
    }

    /** Set the global lcoation mapper. (as returned by get())
     * If called before any call to get(), then the usual default global location mapper is not created 
     * @param globalLocationMapper
     */
    public static void setGlobalLocationMapper(LocationMapper2 globalLocationMapper)
    {
        theMapper = globalLocationMapper ;
    }

    /** Make a location mapper from the path settings */ 
    static public LocationMapper2 makeGlobal()
    {
        LocationMapper2 lMap = new LocationMapper2() ;
        if ( getGlobalConfigPath() != null )
        {
            LocationMapper2 lMap2 = JenaIOEnvironment.createLocationMapper(getGlobalConfigPath()) ;
            lMap.copyFrom(lMap2) ;
        }
        return lMap ;
    }
  
    /** Create a LocationMapper based on Model */
    public static LocationMapper2 processConfig(Model m)
    {
        LocationMapper2 locMap = new LocationMapper2() ; 
        StmtIterator mappings =
            m.listStatements(null, LocationMappingVocab.mapping, (RDFNode)null) ;
    
        for (; mappings.hasNext();)
        {
            Statement s = mappings.nextStatement() ;
            Resource mapping =  s.getResource() ;
            
            if ( mapping.hasProperty(LocationMappingVocab.name) )
            {
                try 
                {
                    String name = mapping.getRequiredProperty(LocationMappingVocab.name)
                                        .getString() ;
                    String altName = mapping.getRequiredProperty(LocationMappingVocab.altName)
                                        .getString() ;
                    locMap.addAltEntry(name, altName) ;
                    log.debug("Mapping: "+name+" => "+altName) ;
                } catch (JenaException ex)
                {
                    log.warn("Error processing name mapping: "+ex.getMessage()) ;
                    throw ex ;
                }
            }
            
            if ( mapping.hasProperty(LocationMappingVocab.prefix) )
            {
                try 
                {
                    String prefix = mapping.getRequiredProperty(LocationMappingVocab.prefix)
                                        .getString() ;
                    String altPrefix = mapping.getRequiredProperty(LocationMappingVocab.altPrefix)
                                        .getString() ;
                    locMap.addAltPrefix(prefix, altPrefix) ;
                    log.debug("Prefix mapping: "+prefix+" => "+altPrefix) ;
                } catch (JenaException ex)
                {
                    log.warn("Error processing prefix mapping: "+ex.getMessage()) ;
                    throw ex ;
                }
            }
        }
        return locMap ;
    }

    /** Search a path (which is delimited by ";" because ":" is used in URIs)
     *  to find a description of a LocationMapper, then create and return a
     *  LocationMapper based on the description.
     */
    public static LocationMapper2 createLocationMapper(String configPath)
    {
        // Old code : maintenance: use Webreader to open the model. 
        
        LocationMapper2 locMap = new LocationMapper2() ;
        if ( configPath == null || configPath.length() == 0 )
        {
            log.warn("Null configuration") ;
            return null ;
        }
        
        // Make a file manager to look for the location mapping file
        StreamManager smgr = new StreamManager() ;
        smgr.addLocator(new LocatorFile2()) ;
        smgr.addLocator(new LocatorClassLoader(smgr.getClass().getClassLoader())) ;
        
        try {
            String uriConfig = null ; 
            TypedInputStream2 in = null ;
            
            StringTokenizer pathElems = new StringTokenizer( configPath, AdapterFileManager.PATH_DELIMITER );
            while (pathElems.hasMoreTokens()) {
                String uri = pathElems.nextToken();
                if ( uri == null || uri.length() == 0 )
                    break ;
                
                in = smgr.openNoMapOrNull(uri) ;
                if ( in != null )
                {
                    uriConfig = uri ;
                    break ;
                }
            }
    
            if ( in == null )
            {
                log.debug("Failed to find configuration: "+configPath) ;
                return null ;
            }
            String syntax = FileUtils.guessLang(uriConfig) ;
            Model model = ModelFactory.createDefaultModel() ;
            model.read(in.getInput(), uriConfig, syntax) ;
            processConfig(model) ;
        } catch (JenaException ex)
        {
            LoggerFactory.getLogger(LocationMapper2.class).warn("Error in configuration file: "+ex.getMessage()) ;
        }
        return locMap ;
    }

}