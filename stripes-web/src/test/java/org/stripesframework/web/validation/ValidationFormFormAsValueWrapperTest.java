package org.stripesframework.web.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.stripesframework.web.FilterEnabledTestBase;
import org.stripesframework.web.action.ActionBean;
import org.stripesframework.web.action.ActionBeanContext;
import org.stripesframework.web.action.DefaultHandler;
import org.stripesframework.web.action.HandlesEvent;
import org.stripesframework.web.action.Resolution;
import org.stripesframework.web.action.UrlBinding;
import org.stripesframework.web.mock.MockRoundtrip;


/**
 * Test out various aspects of the validation subsystem in Stripes with regard to optional
 * application, ordering and flow control. Each of the individual test methods has javadoc
 * explaining why the expected results are as they are.
 *
 * @author Olaf Stracke
 */
@UrlBinding("/test/ValidationFormFormAsValueWrapperTest.action")
public class ValidationFormFormAsValueWrapperTest extends FilterEnabledTestBase implements ActionBean {

   private ActionBeanContext          _context;
   private BusinessValueFormAsWrapper _businessValue1;

   @BeforeEach
   public void before() {
      BusinessValueFormAsWrapper.counter.set(1);
      BusinessValueFormAsWrapper.validateAlwaysRan.set(0);
      BusinessValueFormAsWrapper.validateOneRan.set(0);
      BusinessValueFormAsWrapper.validateTwoRan.set(0);
   }

   @ValidateForm(on = "eventOne")
   public BusinessValueFormAsWrapper getBusinessValue1() {
      if ( _businessValue1 == null ) {
         _businessValue1 = new BusinessValueFormAsWrapper();
      }
      return _businessValue1;
   }

   @Override
   public ActionBeanContext getContext() { return _context; }

   @DefaultHandler
   @HandlesEvent("eventOne")
   public Resolution one() { return null; }

   @Override
   public void setContext( ActionBeanContext context ) { _context = context;}

   /**
    * Almost identical to testEventOneWithNoErrors except that we invoke the 'default' event.
    * Tests to make sure that event-specific validations are still applied correctly when the
    * event name isn't present in the request.
    */
   @Test
   public void testEventOneAsDefault() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.addParameter("businessValue1.numberZero", "100");
      trip.addParameter("businessValue1.numberOne", "101");
      trip.execute();

      ValidationFormFormAsValueWrapperTest test = trip.getActionBean(getClass());
      assertThat(BusinessValueFormAsWrapper.validateAlwaysRan.get().intValue()).isEqualTo(1);
      assertThat(BusinessValueFormAsWrapper.validateOneRan.get().intValue()).isEqualTo(2);
      assertThat(BusinessValueFormAsWrapper.validateTwoRan.get().intValue()).isEqualTo(3);
      assertThat(test.getBusinessValue1().getNumberZero()).isEqualTo(100);
      assertThat(test.getBusinessValue1().getNumberOne()).isEqualTo(101);
      assertThat(test.getContext().getValidationErrors()).isEmpty();
   }

   /**
    * Number one is a required field also for this event, so we supply it.  This event should
    * cause both validateAlways and validateOne to run.
    */
   @Test
   public void testEventOneNoErrors() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.addParameter("businessValue1.numberZero", "100");
      trip.addParameter("businessValue1.numberOne", "101");

      trip.execute("eventOne");

      ValidationFormFormAsValueWrapperTest test = trip.getActionBean(getClass());
      assertThat(BusinessValueFormAsWrapper.validateAlwaysRan.get().intValue()).isEqualTo(1);
      assertThat(BusinessValueFormAsWrapper.validateOneRan.get().intValue()).isEqualTo(2);
      assertThat(BusinessValueFormAsWrapper.validateTwoRan.get().intValue()).isEqualTo(3);
      assertThat(test.getBusinessValue1().getNumberZero()).isEqualTo(100);
      assertThat(test.getBusinessValue1().getNumberOne()).isEqualTo(101);
      assertThat(test.getContext().getValidationErrors()).isEmpty();
   }

   @Test
   public void testEventOneWithEmptyFields() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.addParameter("businessValue1.numberZero", ""); // required field always
      trip.addParameter("businessValue1.numberTwo", "");  // required field for event one
      trip.execute("eventOne");

      ValidationFormFormAsValueWrapperTest test = trip.getActionBean(getClass());
      assertThat(BusinessValueFormAsWrapper.validateAlwaysRan.get().intValue()).isEqualTo(0);
      assertThat(BusinessValueFormAsWrapper.validateOneRan.get().intValue()).isEqualTo(0);
      assertThat(BusinessValueFormAsWrapper.validateTwoRan.get().intValue()).isEqualTo(1);
      assertThat(test.getContext().getValidationErrors()).hasSize(2);
   }

   /**
    * Tests that a required field error is raised this time for numberOne which is only
    * required for this event.  Again this single error should prevent both validateAlways
    * and validateOne from running.
    */
   @Test
   public void testEventOneWithErrors() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.addParameter("businessValue1.numberZero", "100");
      trip.addParameter("businessValue1.numberOne", "");  // required field for event one
      trip.execute("eventOne");

      ValidationFormFormAsValueWrapperTest test = trip.getActionBean(getClass());
      assertThat(BusinessValueFormAsWrapper.validateAlwaysRan.get().intValue()).isEqualTo(0);
      assertThat(BusinessValueFormAsWrapper.validateOneRan.get().intValue()).isEqualTo(0);
      assertThat(BusinessValueFormAsWrapper.validateTwoRan.get().intValue()).isEqualTo(1);
      assertThat(test.getBusinessValue1().getNumberZero()).isEqualTo(100);
      assertThat(test.getContext().getValidationErrors()).hasSize(1);
   }

   /**
    * Straightforward test for event two that makes sure it's validations run.  This time
    * numberTwo should be required (and is supplied) and validateAlways and validateTwo should
    * run but not validateOne.
    */
   @Test
   public void testEventTwoNoErrors() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.addParameter("businessValue1.numberZero", "100");
      trip.addParameter("businessValue1.numberTwo", "102");

      trip.execute("eventTwo");

      ValidationFormFormAsValueWrapperTest test = trip.getActionBean(getClass());
      assertThat(BusinessValueFormAsWrapper.validateAlwaysRan.get().intValue()).isEqualTo(0);
      assertThat(BusinessValueFormAsWrapper.validateOneRan.get().intValue()).isEqualTo(0);
      assertThat(BusinessValueFormAsWrapper.validateTwoRan.get().intValue()).isEqualTo(0);
      assertThat(test._businessValue1.getNumberZero()).isEqualTo(100);
      assertThat(test._businessValue1.getNumberTwo()).isEqualTo(102);
      assertThat(test.getContext().getValidationErrors().size()).isEqualTo(0);
   }

   /**
    * Tests that validateTwo is run event though there are errors and valiateAlways did not run,
    * because validateTwo is marked to run always.
    */
   @Test
   public void testEventTwoWithEmptyFields() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.addParameter("businessValue1.numberZero", ""); // required field always
      trip.addParameter("businessValue1.numberTwo", "");  // required field for event one
      trip.execute("eventTwo");

      ValidationFormFormAsValueWrapperTest test = trip.getActionBean(getClass());
      assertThat(BusinessValueFormAsWrapper.validateAlwaysRan.get().intValue()).isEqualTo(0);
      assertThat(BusinessValueFormAsWrapper.validateOneRan.get().intValue()).isEqualTo(0);
      assertThat(BusinessValueFormAsWrapper.validateTwoRan.get().intValue()).isEqualTo(0);
      assertThat(test.getContext().getValidationErrors()).hasSize(0);
   }

   /**
    * numberZero is the only required field for eventZero, so there should be no validation
    * errors generated. The only validation method that should be run is validateAlways() because
    * the others are tied to specific events.
    */
   @Test
   public void testEventZeroNoErrors() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.addParameter("businessValue1.numberZero", "99");
      trip.execute("eventZero");

      ValidationFormFormAsValueWrapperTest test = trip.getActionBean(getClass());
      assertThat(BusinessValueFormAsWrapper.validateAlwaysRan.get().intValue()).isEqualTo(0);
      assertThat(BusinessValueFormAsWrapper.validateOneRan.get().intValue()).isEqualTo(0);
      assertThat(BusinessValueFormAsWrapper.validateTwoRan.get().intValue()).isEqualTo(0);
      assertThat(test.getContext().getValidationErrors()).isEmpty();
   }

   /**
    * Generates an error by providing an invalid value for numberOne (which has a minimum
    * value of 0).  Validations other than required are still applied even though that @Validate
    * has a on="one".  The single validaiton error should prevent validateAlways() and
    * validateOne from running.
    */
   @Test
   public void testEventZeroWithErrors() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.addParameter("businessValue1.numberZero", "99");
      trip.addParameter("businessValue1.numberOne", "-100");
      trip.execute("eventZero");

      ValidationFormFormAsValueWrapperTest test = trip.getActionBean(getClass());
      assertThat(BusinessValueFormAsWrapper.validateAlwaysRan.get().intValue()).isEqualTo(0);
      assertThat(BusinessValueFormAsWrapper.validateOneRan.get().intValue()).isEqualTo(0);
      assertThat(BusinessValueFormAsWrapper.validateTwoRan.get().intValue()).isEqualTo(0);
      assertThat(test.getBusinessValue1().getNumberZero()).isEqualTo(99);
      assertThat(test.getContext().getValidationErrors()).hasSize(1);
   }

   @HandlesEvent("eventTwo")
   @SuppressWarnings("unused")
   public Resolution two() { return null; }

   @HandlesEvent("eventZero")
   @SuppressWarnings("unused")
   public Resolution zero() { return null; }

}
