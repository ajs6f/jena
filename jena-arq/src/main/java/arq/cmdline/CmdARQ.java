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

package arq.cmdline;

import jena.cmd.ArgDecl;
import jena.cmd.CmdGeneral;
import jena.cmd.ModVersion;

import org.apache.jena.Jena;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.riot.RIOT ;
import org.apache.jena.sparql.engine.iterator.QueryIteratorBase ;

public abstract class CmdARQ extends CmdGeneral
{
	static { ARQ.init() ; }
	

    protected ModVersion modVersion = new ModVersion(true) ;
    
    protected ModSymbol modSymbol = new ModSymbol() ;
    ArgDecl  strictDecl = new ArgDecl(ArgDecl.NoValue, "strict") ;
    
    protected boolean cmdStrictMode = false ; 
    
    protected CmdARQ(String[] argv)
    {
        super(argv) ;
        addModule(modSymbol) ;
        addModule(modVersion) ;
        modVersion.addClass(Jena.class) ;
        modVersion.addClass(ARQ.class) ;
        modVersion.addClass(RIOT.class) ;
        super.add(strictDecl, "--strict", "Operate in strict SPARQL mode (no extensions of any kind)") ; 
    }
    
    @Override
    protected void processModulesAndArgs()
    { 
        if ( modVersion.getVersionFlag() )
            modVersion.printVersionAndExit() ;
        if ( super.contains(strictDecl) ) 
            ARQ.setStrictMode() ;
        cmdStrictMode = super.contains(strictDecl) ;
        if ( modGeneral.debug )
            QueryIteratorBase.traceIterators = true ;
    }
    
    @Override
    protected String getCommandName()
    {
        return Lib.className(this) ;
    }
}
