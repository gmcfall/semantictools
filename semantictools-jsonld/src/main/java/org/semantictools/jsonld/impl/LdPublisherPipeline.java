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
package org.semantictools.jsonld.impl;

import org.semantictools.jsonld.LdAsset;
import org.semantictools.jsonld.LdPublishException;
import org.semantictools.jsonld.LdPublisher;

public class LdPublisherPipeline implements LdPublisher {
  
  private LdPublisher primaryPublisher;
  private LdPublisher secondaryPublisher;
  
  

  public LdPublisherPipeline(LdPublisher primaryPublisher,
      LdPublisher secondaryPublisher) {
    this.primaryPublisher = primaryPublisher;
    this.secondaryPublisher = secondaryPublisher;
  }



  @Override
  public void publish(LdAsset asset) throws LdPublishException {
    primaryPublisher.publish(asset);
    if (secondaryPublisher != null) {
      secondaryPublisher.publish(asset);
    }

  }

}
