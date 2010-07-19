package org.jvnet.hk2.test.impl;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.test.contracts.TestingInfoService;

/**
 * Testing class for recursive relationships.
 * 
 * @author Jeff Trent
 */
@Service(name="RecursiveA2B2OptA")
public class RecursiveA2B2OptA implements TestingInfoService, PostConstruct {

  @Inject(name="RecursiveB2OptA")
  TestingInfoService b;
  
  boolean postConstruct;
  boolean preDestroyed;
  
  public TestingInfoService getB() {
    return b;
  }
  
  @Override
  public boolean isPreDestroyed() {
    return preDestroyed;
  }

  @Override
  public void preDestroy() {
    preDestroyed = true;
  }

  @Override
  public void postConstruct() {
    postConstruct = true;
  }
  
  public boolean isPostConstruct() {
    return postConstruct;
  }

}
