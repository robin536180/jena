/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */
 
package org.seaborne.tdb2.loader;

import org.apache.jena.atlas.logging.FmtLog ;
import org.apache.jena.atlas.logging.ProgressLogger ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;
import org.seaborne.tdb2.lib.TDBTxn ;
import org.seaborne.tdb2.store.DatasetGraphTDB ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class Loader {
    // XXX StreamRDFBatchSplit and parallel index update.
    private static Logger LOG = LoggerFactory.getLogger("Loader") ;
    
    public static void bulkLoad(Dataset ds, String ... files) {
        DatasetGraphTDB dsg = (DatasetGraphTDB)ds.asDatasetGraph() ;
        StreamRDF s1 = StreamRDFLib.dataset(dsg) ;
        ProgressLogger plog = new ProgressLogger(LOG, "Triples", 100000, 10) ;
        StreamRDFMonitor sMonitor = new StreamRDFMonitor(s1, plog) ;
        StreamRDF s3 = sMonitor ;

        sMonitor.startMonitor(); 
        TDBTxn.executeWrite(ds, () -> {
            for ( String fn : files ) {
                if ( files.length > 1 )
                    FmtLog.info(LOG, "File: %s",fn);
                RDFDataMgr.parse(s3, fn) ;
            }
        }) ;
        sMonitor.finishMonitor();  
    }
    
    public static void bulkLoadBatching(Dataset ds, String ... files) {
        DatasetGraphTDB dsg = (DatasetGraphTDB)ds.asDatasetGraph() ;

        StreamRDFBatchSplit s1 = new StreamRDFBatchSplit(dsg, 10) ;
        ProgressLogger plog = new ProgressLogger(LOG, "Triples", 100000, 10) ;
        // Want the monitor on the outside to capture transaction wrapper costs.
        StreamRDFMonitor sMonitor = new StreamRDFMonitor(s1, plog) ;
        StreamRDF s3 = sMonitor ;

        sMonitor.startMonitor(); 
        TDBTxn.executeWrite(ds, () -> {
            for ( String fn : files ) {
                if ( files.length > 1 )
                    FmtLog.info(LOG, "File: %s",fn);
                RDFDataMgr.parse(s3, fn) ;
            }
        }) ;
        sMonitor.finishMonitor();  
    }
}

