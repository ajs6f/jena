/**
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

package org.apache.jena.sparql.core.mem;

import org.apache.jena.atlas.lib.Timer ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.lang.StreamRDFCounting ;
import org.apache.jena.riot.system.StreamRDFLib ;
import org.apache.jena.sparql.core.DatasetGraphWithLock ;
import org.junit.Test;

public class PerfTest {

    @Test
    public  void testPerf() {
        final String data = "/Users/ajs6f/Documents/jena/bsbm-1m.nt.gz" ;
        //final String data = "/home/afs/Datasets/BSBM/bsbm-250k.nt.gz" ;

        final Runnable dsgInMemAuto = ()-> {
            final Dataset ds = DatasetFactory.create(new DatasetGraphInMemory()) ;
            RDFDataMgr.read(ds, data) ;
        } ;
        final Runnable dsgInMemTxn = ()-> {
            final Dataset ds = DatasetFactory.create(new DatasetGraphInMemory()) ;
            ds.begin(ReadWrite.WRITE) ;
            RDFDataMgr.read(ds, data) ;
            ds.commit() ;
            ds.end();
        } ;


        final Runnable plainInMemAuto = ()-> {
            final Dataset ds = DatasetFactory.createMem() ;
            RDFDataMgr.read(ds, data) ;
        } ;
        final Runnable plainInMemTxn = ()-> {
            Dataset ds = DatasetFactory.createMem() ;
            ds = DatasetFactory.create(new DatasetGraphWithLock(ds.asDatasetGraph()) ) ;
            ds.begin(ReadWrite.WRITE) ;
            RDFDataMgr.read(ds, data) ;
            ds.commit() ;
            ds.end();
        } ;

        System.out.printf("==== Data: %s ====\n", data) ;
        // Warm filing system cache and parser if needed.
        //RDFDataMgr.parse(StreamRDFLib.sinkNull(), data);

        final StreamRDFCounting c = StreamRDFLib.count() ;
        final Timer timer = new Timer() ;
        timer.startTimer();
        RDFDataMgr.parse(c, data);
        final long z = timer.endTimer() ;
        final long dataSize = c.count() ;
        final long rate = (1000*dataSize)/z ;
        System.out.printf("    Size: %,d (%ss, %,d tps)\n", dataSize, Timer.timeStr(z), rate) ;

        final int warmN = 3 ;
        final int runN = 20 ;
        final boolean verbose = false ;

        if ( warmN > 0 ) {
            time(false, "DSG/mix/auto", data, dataSize, warmN, verbose, plainInMemAuto);
            time(false, "DSG/mix/txn ", data, dataSize, warmN, verbose, plainInMemTxn);
            time(false, "DSG/mem/auto", data, dataSize, warmN, verbose, dsgInMemAuto);
            time(false, "DSG/mem/txn ", data, dataSize, warmN, verbose, dsgInMemTxn);
        }

        if ( runN > 0 ) {
            time(true, "DSG/mix/auto", data, dataSize, runN, verbose, plainInMemAuto);
            time(true, "DSG/mix/txn ", data, dataSize, runN, verbose, plainInMemTxn);
            time(true, "DSG/mem/auto", data, dataSize, runN, verbose, dsgInMemAuto);
            time(true, "DSG/mem/txn ", data, dataSize, runN, verbose, dsgInMemTxn);
        }

        System.out.println("---------------------") ;
        System.exit(0) ;
    }

    public static void time(final boolean timeForReal, final String label, final String data, final long dataSize, final int N , final boolean verbose, final Runnable r) {
        Runtime.getRuntime().gc() ;
        if ( timeForReal )
            System.out.printf("==== %s (N=%d)\n", label, N) ;
        else
            System.out.printf("==== %s (warm N=%d)\n", label, N) ;
        final Timer timer = new Timer() ;
        timer.startTimer();
        long z = 0 ;
        for ( int i = 1 ; i < N+1 ; i++ ) {
            r.run() ;
            final long z1 = timer.readTimer() ;
            if ( timeForReal && verbose ) System.out.printf("    (%d of %d) Time: %ss\n",  i, N, Timer.timeStr(z1-z)) ;
            z = z1 ;
        }
        final long total = timer.endTimer() ;
        final long rate = (1000*N*dataSize)/total ;
        if ( timeForReal ) System.out.printf("==== %s (N=%d) Time: %ss (%,d tps)\n", label, N, Timer.timeStr(total), rate) ;
    }
}

