package org.stripesframework.web.action;

public interface SingleBeanForm<T> extends ActionBean {

   T getBean();

   void setBean( T initial );
}
