package net.sourceforge.stripes.action;

public interface SingleBeanForm<T> extends ActionBean {

   T getBean();

   void setBean( T initial );
}
