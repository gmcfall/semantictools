/*******************************************************************************
 * Copyright 2012 Pearson Education
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.semantictools.jsonld.io;

import java.io.IOException;
import java.io.InputStream;

import org.semantictools.jsonld.LdNode;

/**
 * LdParser is an interface used to parse a JSON-LD document.
 * The LdParser may be configured to be streaming or non-streaming.
 * 
 * @author Greg McFall
 *
 */
public interface LdParser {
  
  /**
   * Returns the top-level node parsed from the input stream.
   * If this parser is a {@link #setStreaming(boolean) streaming} parser, then
   * the returned node will not be pre-populated with its contents.  In this case,
   * there returned node will still be connected to the parser, and the contents
   * of the node will be parsed only on demand.  Indeed, in this case, the caller
   * must iterate over all fields in object nodes and all elements in container nodes
   * in order to complete the parse.  Furthermore, the nodes returned from a streaming 
   * parser are designed as "read-once" objects.  This means that you can obtain an iterator
   * from each node only once when a streaming parser is used.
   * <p>
   * On the other hand, a non-streaming parser will parse the entire JSON-LD document into memory
   * and return a node that is completely disconnected from the parser.
   */
  LdNode parse(InputStream input) throws LdParseException, IOException;

  /**
   * Specify whether or not this parser is a streaming parser.
   * @param streaming true indicates that the parser is a streaming parser, and false indicates 
   * a non-streaming parser.
   */
  void setStreaming(boolean streaming);
  
  /**
   * Returns true if this parser is a streaming parser and false otherwise.
   */
  boolean isStreaming();

}
