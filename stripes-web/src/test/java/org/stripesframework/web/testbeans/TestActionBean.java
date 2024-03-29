package org.stripesframework.web.testbeans;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.stripesframework.web.action.ActionBean;
import org.stripesframework.web.action.ActionBeanContext;
import org.stripesframework.web.action.DefaultHandler;
import org.stripesframework.web.action.UrlBinding;
import org.stripesframework.web.validation.LongTypeConverter;
import org.stripesframework.web.validation.Validate;
import org.stripesframework.web.validation.ValidateNestedProperties;


/**
 * An ActionBean that has a fairly complete/complex set of properties in order to
 * help out in binding tests.
 *
 * @author Tim Fennell
 */
@UrlBinding("/test/Test.action")
public class TestActionBean implements ActionBean {

   private ActionBeanContext   context;
   private List<Long>          listOfLongs;
   private Set<String>         setOfStrings;
   private List<TestBean>      listOfBeans;
   private Map<String, Long>   mapOfLongs;
   private Map<String, Object> mapOfObjects;
   private TestBean            testBean;
   private String              singleString;
   private Long                singleLong;
   @SuppressWarnings("unchecked")
   private List                nakedListOfLongs;
   private int[]               intArray;
   private String              setOnlyString;
   @Validate
   public  Long                publicLong;
   public  Color[]             colors;
   private PropertyLess        item = new Item();

   @DefaultHandler
   public void doNothing() { /* Do Nothing. */ }

   /** An array of enums to test out enums, and arrays of non-instantiable things. */
   public Color[] getColors() { return colors; }

   @Override
   public ActionBeanContext getContext() { return context; }

   /** An array of primitive ints to test out Array binding. */
   public int[] getIntArray() { return intArray; }

   /** Return type is a property-less class, but returns an instance of a subclass with an 'id' property. */
   public PropertyLess getItem() { return item; }

   /** A list of TestBean objects to test indexed and nested properties. */
   public List<TestBean> getListOfBeans() { return listOfBeans; }

   /** A pretty ordinary list of longs, to test lists of primitive/simply objects. */
   public List<Long> getListOfLongs() { return listOfLongs; }

   /** A map of longs to test Maps of type converted properties. */
   public Map<String, Long> getMapOfLongs() { return mapOfLongs; }

   /** A map of Objects which should get populated as Strings because String extends Object. */
   public Map<String, Object> getMapOfObjects() { return mapOfObjects; }

   /** A non-generic list to make sure specifying the converter is used properly. */
   @SuppressWarnings("unchecked")
   public List getNakedListOfLongs() { return nakedListOfLongs; }

   /** A Set of Strings, to test non List based collections. */
   public Set<String> getSetOfStrings() { return setOfStrings; }

   /** Just a basic Long property. */
   public Long getSingleLong() { return singleLong; }

   /** Just a basic string property. */
   public String getSingleString() { return singleString; }

   /** A single test bean to test out basic nested properties. */
   public TestBean getTestBean() { return testBean; }

   @Validate
   public void setColors( Color[] colors ) { this.colors = colors; }

   ///////////////////////////////////////////////////////////////////////////
   // Dummied up ActionBean methods that aren't really used for much.
   ///////////////////////////////////////////////////////////////////////////
   @Override
   public void setContext( ActionBeanContext context ) { this.context = context; }

   @Validate
   public void setIntArray( int[] intArray ) { this.intArray = intArray; }

   @ValidateNestedProperties({ //
                               @Validate(field = "id"), //
                             })
   public void setItem( PropertyLess item ) { this.item = item; }

   @ValidateNestedProperties({  //
                                @Validate(field = "intProperty"), //
                             })
   public void setListOfBeans( List<TestBean> listOfBeans ) { this.listOfBeans = listOfBeans; }

   @Validate
   public void setListOfLongs( List<Long> listOfLongs ) { this.listOfLongs = listOfLongs; }

   @Validate
   public void setMapOfLongs( Map<String, Long> mapOfLongs ) { this.mapOfLongs = mapOfLongs; }

   @Validate
   public void setMapOfObjects( Map<String, Object> mapOfObjects ) { this.mapOfObjects = mapOfObjects; }

   @Validate(converter = LongTypeConverter.class)
   public void setNakedListOfLongs( List nakedListOfLongs ) { this.nakedListOfLongs = nakedListOfLongs; }

   @Validate
   public boolean setOnlyStringIsNotNull() { return setOnlyString != null; }

   @Validate
   public void setSetOfStrings( Set<String> setOfStrings ) { this.setOfStrings = setOfStrings; }

   /** A property with only a setter to test out setting when there's no getter. */
   @Validate
   public void setSetOnlyString( String setOnlyString ) { this.setOnlyString = setOnlyString; }

   @Validate
   public void setSingleLong( Long singleLong ) { this.singleLong = singleLong; }

   @Validate
   public void setSingleString( String singleString ) { this.singleString = singleString; }

   @ValidateNestedProperties({  //
                                @Validate(field = "stringProperty"), //
                                @Validate(field = "intProperty"), //
                                @Validate(field = "longProperty"), //
                                @Validate(field = "enumProperty"), //
                                @Validate(field = "booleanProperty"), //
                                @Validate(field = "genericArray"), //
                                @Validate(field = "stringArray"), //
                                @Validate(field = "stringList"), //
                                @Validate(field = "stringSet"), //
                                @Validate(field = "stringMap"), //
                                @Validate(field = "nestedMap"), //
                                @Validate(field = "longMap"), //
                             })
   public void setTestBean( TestBean testBean ) { this.testBean = testBean; }

   public enum Color {
      Red,
      Green,
      Blue,
      Yellow,
      Orange,
      Black,
      White
   }


   public static class Item extends PropertyLess {

      private Long id;

      public Long getId() { return id; }

      public void setId( Long id ) { this.id = id; }
   }


   /** Pair of static classes used to check type finding via instances intead of type inference. */
   public static class PropertyLess {}
}
