package tester;

//import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.io.*;

//import org.apache.log4j.Logger;

/**
 * Copyright 2007, 2008, 2009 Viera K. Proulx, Weston Josey
 * This program is distributed under the terms of the 
 * GNU Lesser General Public License (LGPL)
 */

/**
 * <P>
 * A test harness that compares arbitrary objects for extensional equality.
 * </P>
 * <P>
 * It catches exceptions in tests - the remaining tests are omitted.
 * </P>
 * <P>
 * It uses the visitor pattern to accept test cases.
 * </P>
 * <P>
 * Displays the data even if no tests are present.
 * </P>
 * 
 * @author Viera K. Proulx, Weston Jossey
 * @since 21 March 2008, 11 June 2008, 24 June 2008, 16 October 2008, 11
 *        November 2008, 23 November 2008, 12 December 2008, 20 January 2009
 */
public class Tester {
    // private static final Logger logger = Logger.getLogger(Tester.class);

    /** A <code>String</code> that records the results for all failed tests */
    protected String testResults = "Test results: \n--------------\n";

    /** A <code>String</code> that records all test results */
    protected StringBuilder fullTestResults = new StringBuilder(
            "Full test results: " + "\n-------------------\n");

    /** the total number of tests */
    protected int numberOfTests;

    /** the total number of errors */
    protected int errors;

    /** the name of the current test */
    protected String testname;

    /** An instance of the Inspector to use throughout */
    protected Inspector inspector = new Inspector();

    /** start with no tests and no failures */
    protected Tester() {
        this.numberOfTests = 0;
        this.errors = 0;
        this.testname = "";
    }

    /*--------------------------------------------------------------------*/
    /*------------- TEST SELECTION AND REPORTING SECTION -----------------*/
    /*--------------------------------------------------------------------*/
    /**
     * <p>
     * Super magic happens here....
     * </p>
     * <p>
     * Report only the results of all failed tests in the class whose instance
     * is given as the parameter.
     * </p>
     * 
     * @param f
     *            the instance of a class that defines all tests and the data
     *            for them
     */
    protected void runAnyTests(Object f) {
        runAnyTests(f, false);
    }

    /**
     * <p>
     * Super magic happens here....
     * </p>
     * <p>
     * Report only the results of tests in the class whose instance is given as
     * the parameter.
     * </p>
     * <p>
     * If the class extends the <code>IExamples</code> all tests within the
     * <code>test</code> method are performed. Otherwise we locate all methods
     * with names that start with <code>test</code> and consume one parameter
     * of the type <code>Tester</code> and perform the desired tests.
     * </p>
     * 
     * @param f
     *            the instance of a class that defines all tests and the data
     *            for them
     * @param full
     *            true if full test report is desired
     */
    protected void runAnyTests(Object f, boolean full) {
        runAnyTests(f, full, false);
    }

    /**
     * <p>
     * Super magic happens here....
     * </p>
     * <p>
     * Report only the results of tests in the class whose instance is given as
     * the parameter.
     * </p>
     * <p>
     * If the class extends the <code>IExamples</code> all tests within the
     * <code>test</code> method are performed. Otherwise we locate all methods
     * with names that start with <code>test</code> and consume one parameter
     * of the type <code>Tester</code> and perform the desired tests.
     * </p>
     * 
     * @param f
     *            The instance of a class that defines all tests and the data
     *            for them
     * @param full
     *            true if full test report is desired
     * @param printall
     *            true if <code>Examples</code> class data is to be printed
     */
    protected void runAnyTests(Object f, boolean full, boolean printall) {
        this.numberOfTests = 0; // number of tests run
        boolean failed = false; // any tests failed?

        if (printall) {
            // pretty-print the 'Examples' class data when desired
            System.out.println(f.getClass().getName() + ":\n---------------");
            System.out.println(Printer.produceString(f) + "\n---------------");
        }

        // check if the 'Examples' class extends 'IExamples'
        // if yes, run its 'tests' method
        Class<?>[] interfaces = f.getClass().getInterfaces();
        boolean foundInterface = false;

        for (Class<?> c : interfaces) {
            if (c.getName().equals("tester.IExamples")) {
                runTests((IExamples) f);
                foundInterface = true;
            }
        }

        // otherwise use reflection to find all test methods
        if (!foundInterface) {

            // find all methods that start with the String 'test...'
            // and consume one argument of the type Tester
            // Never used- Wes Jossey
            // Class<?>[] classparams = new Class[] { this.getClass() };

            ArrayList<Method> testMethods = this.findTestMethods(f,
                    "Your class does not define any method with the header\n"
                            + " boolean test...(Tester t)");

            // make sure there are tests to run
            if (!(testMethods == null)) {
                // invoke every method that starts with 'test'
                // and accepts one Tester argument (ignore the resulting
                // boolean)
                Object[] args = new Object[] { this };

                try {
                    for (Method testMethod : testMethods) {
                        if (testMethod != null) {
                            testMethod.invoke(f, args);
                        }
                    }
                }

                // catch all exceptions
                catch (Throwable e) {
                    this.errors = this.errors + 1;
                    System.out.println("Threw exception during test "
                            + this.numberOfTests);
                    e.printStackTrace();
                    failed = true;
                }

                // print the test results at the end
                finally {
                    if (full)
                        this.fullTestReport();
                    else
                        this.testReport();
                    done(failed);
                }
            }
        }
    }

    /**
     * <P>
     * Run the tests, accept the class to be tested as a visitor
     * </P>
     * <P>
     * An alternative method for running all tests.
     * </p>
     * 
     * @param f
     *            the class to be tested -- it defines the method
     *            <code>void tests(Tester t)</code> that invokes all test
     *            cases.
     */
    protected void runTests(IExamples f) {
        runTests(f, false);
    }

    /**
     * <P>
     * Run the tests, accept the class to be tested as a visitor
     * </P>
     * <P>
     * An alternative method for running all tests.
     * </p>
     * 
     * @param f
     *            the class to be tested -- it defines the method
     *            <code>void tests(Tester t)</code> that invokes all test
     *            cases.
     * @param full
     *            true if full test report is desired
     */
    protected void runTests(IExamples f, boolean full) {
        this.numberOfTests = 0;
        boolean failed = false;

        System.out.println("Examples class:\n---------------");
        System.out.println(Printer.produceString(f) + "\n---------------");

        try {
            f.tests(this);
        } catch (Throwable e) { // catch all exceptions
            this.errors = this.errors + 1;
            System.out.println("Threw exception during test "
                    + this.numberOfTests);
            e.printStackTrace();
            failed = true;
        } finally {
            if (full)
                this.fullTestReport();
            else
                this.testReport();
            done(failed);
        }
    }

    /**
     * On completion of all tests print the count of number of tests and the
     * number of failures.
     */
    protected void done(boolean failed) {
        if (failed) {
            this.reportErrors(testname, "caused RuntimeException", "unkown");
        }
        /*
         * if (this.errors > 0) System.out.print("Test summary:\nFailed " +
         * this.errors + " out of "); else
         * System.out.print("Test summary:\nPassed all "); System.out.println
         * (this.n + " tests.");
         */
    }

    /*--------------------------------------------------------------------*/
    /*------------- TEST EVALUATION SECTION: Public API ------------------*/
    /*--------------------------------------------------------------------*/

    /*--------------- Tolerance for comparing inexact numbers  -----------*/

    /**
     * Set the relative tolerance for the comparison of inexact numbers
     * 
     * @param epsilon
     *            the desired tolerance
     */
    public boolean setTolerance(double epsilon) {
        Inspector.TOLERANCE = epsilon;
        return epsilon > 0;
    }

    /*-------- Delegate the 'same' method to the Inspector class ---------*/

    /**
     * <P>
     * Provide a general extensional equality comparison for arbitrary pair of
     * objects
     * </P>
     * <P>
     * Inspector invokes the user-defined <code>same</code> method, if the
     * corresponding classes implement the <code>ISame</code> interface.
     * </P>
     * 
     * @param obj1
     *            the first object
     * @param obj2
     *            the second object
     * @return true if the two objects represent the same data
     */
    public boolean same(Object obj1, Object obj2) {
        return inspector.isSame(obj1, obj2);
    }

    /*------------------------ Test definitions  -------------------------*/

    public boolean success() {
        return success("");
    }

    /**
     * A simple test that can be used as an easy way of demonstrating a
     * success.
     * 
     * @param testName
     *            the name of the test
     * @return always returns true
     */
    public boolean success(String testName) {
        /*
         * Wrap checkExpect with a true/true combo
         */
        return checkExpect(true, true, testName);
    }

    /**
     * A simple test that can be used as an easy way of demonstrating a
     * failure.
     * 
     * @return always returns false
     */
    public boolean fail() {
        /*
         * Wrap checkExpect with a true/false combo
         */
        return fail("");
    }

    /**
     * A simple test that can be used as an easy way of demonstrating a
     * failure.
     * 
     * @param testName
     *            the name of the test
     * @return always returns false
     */
    public boolean fail(String testName) {
        return checkExpect(true, false, testName);
    }

    /**
     * Test that only reports success or failure
     * 
     * @param result
     *            the test result
     */
    public boolean checkExpect(boolean result) {
        return checkExpect(result, "");
    }

    /**
     * Test that only reports success or failure
     * 
     * @param testname
     *            the name of this test
     * @param result
     *            the test result
     */
    public boolean checkExpect(boolean result, String testname) {
        this.testname = testname;
        if (!result) {
            // add the report of failure
            return this.report(false, testname + ": error -- no blame -- \n",
                    false, true);
        }
        // add the report of success
        else {
            return this.addSuccess(testname + ": success \n");
        }
    }

    /**
     * Test that only reports success or failure
     * 
     * @param result
     *            the test result that should fail
     */
    public boolean checkFail(boolean result) {
        return checkFail(result, "");
    }

    /**
     * Test that only reports success or failure
     * 
     * @param result
     *            the test result that should fail
     */
    public boolean checkFail(boolean result, String testname) {
        return checkExpect(!result, "Failure expected: \n" + testname);
    }

    /*------------------------ <T> vs. <T>  ---------------------------*/

    /**
     * Test that compares two objects of any kind
     * 
     * @param <T>
     *            the type of the objects being compared
     * @param actual
     *            the computed value of the type T
     * @param expected
     *            the expected value of the type T
     */
    public <T> boolean checkExpect(T actual, T expected) {
        return checkExpect(actual, expected, "");
    }

    /**
     * Test that compares two objects of any kind
     * 
     * @param <T>
     *            the type of the objects being compared
     * @param actual
     *            the computed value of the type T
     * @param expected
     *            the expected value of the type T
     * @param testname
     *            the name of this test
     */
    public <T> boolean checkExpect(T actual, T expected, String testname) {
        this.testname = testname;
        return this.report(inspector.isSame(actual, expected), testname,
                actual, expected);
    }

    /**
     * Test that compares two objects of any kind - should fail
     * 
     * @param <T>
     *            the type of the objects being compared
     * @param actual
     *            the computed value of the type T
     * @param expected
     *            the expected value of the type T
     */
    public <T> boolean checkFail(T actual, T expected) {
        return this.checkFail(actual, expected, "");
    }

    /**
     * Test that compares two objects of any kind - should fail
     * 
     * @param <T>
     *            the type of the objects being compared
     * @param actual
     *            the computed value of the type T
     * @param expected
     *            the expected value of the type T
     * @param testname
     *            the name of this test
     */
    public <T> boolean checkFail(T actual, T expected, String testname) {
        this.testname = testname;
        return this.report(!inspector.isSame(actual, expected),
                "Failure expected: \n" + testname, actual, expected);
    }

    /*------------------- Method invocation tests -------------------------*/

    /**
     * Test that verifies that when the given object invokes the given method
     * with the given arguments, it throws the expected exception with the
     * expected message.
     * 
     * @param <T>
     *            The type of the object that invokes the given method
     * @param object
     *            The object that invokes the method to be tested
     * @param method
     *            The name of the method to test
     * @param args
     *            An array of arguments for the method - use Array of length 0
     *            if no arguments are needed
     * @param e
     *            An instance of the expected exception -- including the
     *            expected message
     */
    public <T> boolean checkExpect(T object, String method, Object[] args,
            Exception e) {
        return checkExpect(object, method, args, e, "");
    }

    /**
     * Test that verifies that when the given object invokes the given method
     * with the given arguments, it throws the expected exception with the
     * expected message.
     * 
     * *Note* This is the Ellipsis version of another method
     * 
     * @param <T>
     *            The type of the object that invokes the given method
     * @param e
     *            An instance of the expected exception -- including the
     *            expected message
     * @param object
     *            The object that invokes the method to be tested
     * @param method
     *            The name of the method to test
     * @param args
     *            An <em>Ellipsis</em> of arguments for the method
     */
    public <T> boolean checkExpect(Exception e, T object, String method,
            Object... args) {
        return checkExpect(object, method, args, e);
    }

    /**
     * Test that verifies that when the given object invokes the given method
     * with the given arguments, it throws the expected exception with the
     * expected message.
     * 
     * Note, this is the Ellipsis version.
     * 
     * @param <T>
     *            The type of the object that invokes the given method
     * @param testname
     *            The description of this test
     * @param e
     *            An instance of the expected exception -- including the
     *            expected message
     * @param object
     *            The object that invokes the method to be tested
     * @param method
     *            The name of the method to test
     * @param args
     *            An <em>Ellipsis</em> of arguments for the method
     */
    public <T> boolean checkExpect(String testname, Exception e, T object,
            String method, Object... args) {
        return checkExpect(object, method, args, e, testname);
    }

    /**
     * Test that verifies that when the given object invokes the given method
     * with the given arguments, it throws the expected exception with the
     * expected message.
     * 
     * @param <T>
     *            The type of the object that invokes the given method
     * @param object
     *            The object that invokes the method to be tested
     * @param method
     *            The name of the method to test
     * @param args
     *            An array of arguments for the method - use Array of length 0
     *            if no arguments are needed
     * @param e
     *            An instance of the expected exception -- including the
     *            expected message
     * @param testname
     *            The description of this test
     */
    public <T> boolean checkExpect(T object, String method, Object[] args,
            Exception e, String testname) {
        this.testname = testname;

        // create an Array of the argument types
        int length = (args != null) ? args.length : 0;
        Class<?>[] parameters = new Class[length];

        for (int i = 0; i < length; i++) {
            parameters[i] = args[i].getClass();
        }

        // get the type of the expected exception
        Class<?> exceptClass = e.getClass();
        String exceptName = exceptClass.getName();
        String exceptMessage = e.getMessage();
        try {
            // find the method
            Method meth = this
                    .findMethod(object, method, parameters, testname);

            // allow access to protected and private methods
            meth.setAccessible(true);

            Object result = meth.invoke(object, args);

            // if the invocation succeeds, the test fails because
            // it does not throw the expected exception
            if (result == null)
                return this.report(false, testname
                        + "\n invocation did not throw any exception "
                        + "\n  method name: " + meth.getName()
                        + "\n  object class: " + object.getClass().getName()
                        + "\n  result: void" + ": "
                        + Printer.produceString(result)
                        + "\n  expected exception was: \n    class: "
                        + exceptName + "\n    message: " + exceptMessage
                        + "\n", result, "exception expected");
            else
                return this.report(false, testname
                        + "\n invocation did not throw any exception "
                        + "\n  method name: " + meth.getName()
                        + "\n  object class: " + object.getClass().getName()
                        + "\n  result: " + result.getClass().getName() + ": "
                        + Printer.produceString(result)
                        + "\n  expected exception was: \n    class: "
                        + exceptName + "\n    message: " + exceptMessage
                        + "\n", result, "exception expected");
        } catch (Throwable exception) {

            String excName;
            String excMessage;
            if (exception.getCause() != null) {
                excName = exception.getCause().getClass().getName();
                excMessage = exception.getCause().getMessage();
            } else {
                excName = exception.getClass().getName();
                excMessage = exception.getMessage();
            }

            // check that the method threw an exception of the desired type
            if (excName.equals(exceptName)) {

                // check whether the message is correct
                if (excMessage.equals(exceptMessage)) {

                    // report correct exception, correct message -- test
                    // succeeds
                    return this.addSuccess(testname
                            + "\n correct exception: \n" + "" + " class: "
                            + exceptName + "\n correct message: "
                            + exceptMessage + "\n");
                } else {

                    // report correct exception, incorrect message -- test
                    // fails
                    return this
                            .report(false, testname
                                    + "\n correct exception: \n" + ""
                                    + " class: " + exceptName
                                    + "\n incorrect message: " + excMessage
                                    + "\n", "message produced: " + excMessage,
                                    "message expected: " + exceptMessage);
                }
            } else {
                // report that method invocation threw an exception of the
                // wrong
                // type
                return this.report(false, testname
                        + "\n incorrect exception was thrown: ",
                        "exception thrown:   " + excName,
                        "exception expected: " + exceptName);
            }
        }
    }

    // --------- METHOD INVOCATION TESTS -------------------------------------
    /**
     * Invoke the method with the given name on a given object with the given
     * arguments - check if it produces the expected value
     * 
     * @param <T>
     *            The type of the object that invokes the given method
     * @param object
     *            The object that invokes the method to be tested
     * @param method
     *            The name of the method to test
     * @param args
     *            An array of arguments for the method - use Array of length 0
     *            if no arguments are needed
     * @param expected
     *            The expected result of the method being tested
     */
    public <T> boolean checkExpect(T object, String method, Object[] args,
            Object expected) {
        return checkExpect(object, method, args, expected, "");
    }

    /**
     * Invoke the method with the given name on a given object with the given
     * arguments - check if it produces the expected value
     * 
     * Note: This is the Ellipsis version
     * 
     * @param <T>
     *            The type of the object that invokes the given method
     * @param object
     *            The type of the object that invokes the given method
     * @param method
     *            The name of the method to test
     * @param expected
     *            The expected result of the method being tested
     * @param args
     *            An <em>Ellipsis</em> of arguments for the method
     */
    public <T> boolean checkExpect(Object expected, T object, String method,
            Object... args) {
        return checkExpect(object, method, args, expected, "");
    }

    /**
     * Invoke the method with the given name on a given object with the given
     * arguments - check if it produces the expected value
     * 
     * 
     * Note: This is the Ellipsis version
     * 
     * @param <T>
     *            The type of the object that invokes the given method
     * @param object
     *            The object that invokes the method to be tested
     * @param method
     *            The name of the method to test
     * @param expected
     *            The expected result of the method being tested
     * @param testname
     *            The description of this test
     * @param args
     *            An <em>Ellipsis</em> of arguments for the method
     */
    public <T> boolean checkExpect(String testname, Object expected, T object,
            String method, Object... args) {
        return checkExpect(object, method, args, expected, testname);
    }

    /**
     * Invoke the method with the given name on a given object with the given
     * arguments - check if it produces the expected value
     * 
     * @param <T>
     *            The type of the object that invokes the given method
     * @param object
     *            The object that invokes the method to be tested
     * @param method
     *            The name of the method to test
     * @param args
     *            An array of arguments for the method - use Array of length 0
     *            if no arguments are needed
     * @param expected
     *            The expected result of the method being tested
     * @param testname
     *            The description of this test
     */
    public <T> boolean checkExpect(T object, String method, Object[] args,
            Object expected, String testname) {
        this.testname = testname;
        // create an Array of the argument types
        int length = Array.getLength(args);
        Class<?>[] parameters = new Class[length];

        // Never read- Wes Jossey
        // String[] paraNames = new String[length];

        for (int i = 0; i < length; i++) {
            parameters[i] = args[i].getClass();
        }

        String parlist = "(";
        for (Class<?> parameter : parameters)
            parlist = parlist + (parameter.getName()) + ",";

        parlist = parlist.substring(0, parlist.length() - 1) + ")";

        try {
            // find the method to be invoked by 'object' with the name 'method'
            // and parameters in the classes given by 'parameters'
            Method meth = findMethod(object, method, parameters, testname);

            if (meth == null) {
                return report(false, testname + "\nNo method with the name "
                        + method + " found\n",
                        "Failed to invoke method "
                                + object.getClass().getName() + "." + method
                                + parlist, expected);
            } else {
                // allow access to protected and private methods
                meth.setAccessible(true);

                String testmessage = testname + "\n"
                        + Printer.produceString(object) + "\n invoked method "
                        + method + " in the class "
                        + object.getClass().getName() + "\n with arguments "
                        + Printer.produceString(args) + "\n";

                return checkExpect(meth.invoke(object, args), expected,
                        testmessage);
            }
        } catch (Throwable exception) {
            // String testmessage = testname + "\n" +
            String testmessage = testname + "\n"
                    + Printer.produceString(object) + "\n invoked method "
                    + method + " in the class " + object.getClass().getName()
                    + "\n with arguments " + Printer.produceString(args)
                    + "\n";

            // report that the method threw an exception
            boolean result = this.report(false, testmessage
                    + "\nthrew an excception ", object, Printer
                    .produceString(args));
            exception.printStackTrace();
            return result;
        }
    }

    /**
     * Test that determines whether the value of the given object (of any kind)
     * is the same as one of the expected values
     * 
     * Note: This is the Ellipsis version
     * 
     * @param <T>
     *            The type of the objects being compared
     * @param actual
     *            The computed value of the type T
     * @param expected
     *            An <em>Ellipsis</em> of the expected values of the type T
     */
    public <T> boolean checkOneOf(T actual, T... expected) {
        return checkOneOf(actual, expected, "");
    }

    /**
     * Test that determines whether the value of the given object (of any kind)
     * is the same as one of the expected values
     * 
     * @param <T>
     *            The type of the objects being compared
     * @param actual
     *            The computed value of the type T
     * @param expected
     *            The expected values of the type T
     * @param testname
     *            The name of this test
     */
    public <T> boolean checkOneOf(T actual, T[] expected, String testname) {
        this.testname = testname;
        for (int i = 0; i < Array.getLength(expected); i++) {
            if (inspector.isSame(actual, expected[i]))
                return this.report(true, testname, actual, expected[i]);
        }
        return this.report(false, testname + "\nNo matching value found "
                + "among the list of expected values", actual, expected);
    }

    /**
     * * Test that determines whether the value of the given object (of any
     * kind) is the same as one of the expected values
     * 
     * Note: This is the Ellipsis version
     * 
     * @param <T>
     * @param testname
     *            The name of this test
     * @param actual
     *            The computed value of the type T
     * @param expected
     *            An <em>Ellipsis</em> of the expected values of the type T
     */
    public <T> boolean checkOneOf(String testname, T actual, T... expected) {
        return checkOneOf(actual, expected, testname);
    }

    /*---------------- None of the choices checks -----------------------*/

    /**
     * Test that determines whether the value of the given object (of any kind)
     * is not equal to any of the expected values
     * 
     * @param <T>
     *            the type of the objects being compared
     * @param actual
     *            the computed value of the type T
     * @param expected
     *            the expected values of the type T
     */
    public <T> boolean checkNoneOf(T actual, T... expected) {
        return checkNoneOf(actual, expected, "");
    }

    /**
     * Test that determines whether the value of the given object (of any kind)
     * is not equal to any of the expected values
     * 
     * @param <T>
     *            the type of the objects being compared
     * @param actual
     *            the computed value of the type T
     * @param expected
     *            the expected values of the type T
     */
    public <T> boolean checkNoneOf(String testname, T actual, T... expected) {
        return checkNoneOf(actual, expected, testname);
    }

    /**
     * Test that determines whether the value of the given object (of any kind)
     * is not equal to any of the expected values
     * 
     * @param <T>
     *            the type of the objects being compared
     * @param actual
     *            the computed value of the type T
     * @param expected
     *            the expected values of the type T
     */
    public <T> boolean checkNoneOf(T actual, T[] expected, String testname) {
        this.testname = testname;
        for (int i = 0; i < Array.getLength(expected); i++) {
            if (inspector.isSame(actual, expected[i]))
                return this.report(false,
                        "Matching value found in none of test\n" + testname,
                        actual, expected[i]);
        }
        return this.report(true, testname + "\nNo matching value "
                + "found among the list " + "of excluded values", actual,
                expected);
    }

    /*---------------- Numeric (mixed) range checks -----------------------*/

    /**
     * Test that determines whether the value of the given numerical object (of
     * any kind) is the range between low (inclusive) and high (exclusive)
     * values. Allows for comparison of mixed numeric types.
     * 
     * @param actual
     *            the computed value of the type Number
     * @param low
     *            the lower limit of the type Number (inclusive) for this range
     * @param high
     *            the upper limit of the type Number (exclusive) for this range
     */
    public <T> boolean checkNumRange(Number actual, Number low, Number high) {
        return checkNumRange(actual, low, high, "");
    }

    /**
     * Test that determines whether the value of the given numerical object (of
     * any kind) is the range between low (inclusive) and high (exclusive)
     * values. Allows for comparison of mixed numeric types.
     * 
     * @param actual
     *            the computed value of the type Number
     * @param low
     *            the lower limit of the type Number (inclusive) for this range
     * @param high
     *            the upper limit of the type Number (exclusive) for this range
     */
    public <T> boolean checkNumRange(Number actual, Number low, Number high,
            String testname) {
        return checkNumRange(actual, low, high, true, false, testname);
    }

    /**
     * Test that determines whether the value of the given object (of any kind)
     * is the range between low and high values. User must specify whether the
     * low and high bounds are inclusive or exclusive.
     * 
     * @param actual
     *            the computed value of the type Number
     * @param low
     *            the lower limit of the type Number (inclusive) for this range
     * @param high
     *            the upper limit of the type Number (exclusive) for this range
     * @param lowIncl
     *            is the low limit inclusive for this range?
     * @param highIncl
     *            is the upper limit inclusive for this range?
     */
    public <T> boolean checkNumRange(Number actual, Number low, Number high,
            boolean lowIncl, boolean highIncl) {
        return checkNumRange(actual, low, high, lowIncl, highIncl, "");
    }

    /**
     * Test that determines whether the value of the given object (of any kind)
     * is the range between low and high values. User must specify whether the
     * low and high bounds are inclusive or exclusive.
     * 
     * @param actual
     *            the computed value of the type Number
     * @param low
     *            the lower limit of the type Number (inclusive) for this range
     * @param high
     *            the upper limit of the type Number (exclusive) for this range
     * @param lowIncl
     *            is the low limit inclusive for this range?
     * @param highIncl
     *            is the upper limit inclusive for this range?
     */
    public <T> boolean checkNumRange(Number actual, Number low, Number high,
            boolean lowIncl, boolean highIncl, String testname) {
        this.testname = testname;
        boolean within = true;
        boolean aboveLow = ((Double) actual.doubleValue()).compareTo(low
                .doubleValue()) > 0;
        boolean belowHigh = ((Double) actual.doubleValue()).compareTo(high
                .doubleValue()) < 0;

        if (lowIncl)
            aboveLow = aboveLow
                    || ((Double) actual.doubleValue()).compareTo(low
                            .doubleValue()) == 0;

        if (highIncl)
            belowHigh = belowHigh
                    || ((Double) actual.doubleValue()).compareTo(high
                            .doubleValue()) == 0;

        within = aboveLow && belowHigh;
        // System.out.println("Compared " + actual.toString() +
        // " within [" + low.toString() + ", " + high.toString() + "): " +
        // within);
        if (within)
            return this.report(within, testname, actual, low, high);
        else
            return this.report(within, testname
                    + "\nActual value is not within the [low high) range.",
                    actual, low, high);
    }

    /*-------------------- General range checks --------------------------*/

    /**
     * Test that determines whether the value of the given object (of any kind)
     * is the range between low (inclusive) and high (exclusive) values
     * 
     * @param <T>
     *            the type of the objects being compared
     * @param actual
     *            the computed value of the type T
     * @param low
     *            the lower limit of the type T (inclusive) for this range
     * @param high
     *            the upper limit of the type T (exclusive) for this range
     */
    public <T> boolean checkRange(Comparable<T> actual, T low, T high) {
        return checkRange(actual, low, high, "");
    }

    /**
     * Test that determines whether the value of the given object (of any kind)
     * is the range between low (inclusive) and high (exclusive) values
     * 
     * @param <T>
     *            the type of the objects being compared
     * @param actual
     *            the computed value of the type T
     * @param low
     *            the lower limit of the type T (inclusive) for this range
     * @param high
     *            the upper limit of the type T (exclusive) for this range
     * @param testname
     *            the name of this test
     */
    public <T> boolean checkRange(Comparable<T> actual, T low, T high,
            String testname) {
        return checkRange(actual, low, high, true, false, testname);
    }

    /**
     * Test that determines whether the value of the given object (of any kind)
     * is the range between low and high values. User must specify whether the
     * low and high bounds are inclusive or exclusive.
     * 
     * @param <T>
     *            the type of the objects being compared
     * @param actual
     *            the computed value of the type T
     * @param low
     *            the lower limit of the type T for this range
     * @param high
     *            the upper limit of the type T for this range
     * @param lowIncl
     *            is the low limit inclusive for this range?
     * @param highIncl
     *            is the upper limit inclusive for this range?
     */
    public <T> boolean checkRange(Comparable<T> actual, T low, T high,
            boolean lowIncl, boolean highIncl) {
        return checkRange(actual, low, high, lowIncl, highIncl, "");
    }

    /**
     * Test that determines whether the value of the given object (of any kind)
     * is the range between low and high values. User must specify whether the
     * low and high bounds are inclusive or exclusive.
     * 
     * @param <T>
     *            the type of the objects being compared
     * @param actual
     *            the computed value of the type T
     * @param low
     *            the lower limit of the type T for this range
     * @param high
     *            the upper limit of the type T for this range
     * @param lowIncl
     *            is the low limit inclusive for this range?
     * @param highIncl
     *            is the upper limit inclusive for this range?
     * @param testname
     *            the name of this test
     */
    public <T> boolean checkRange(Comparable<T> actual, T low, T high,
            boolean lowIncl, boolean highIncl, String testname) {
        this.testname = testname;
        boolean within = true;
        boolean aboveLow = true;
        boolean belowHigh = true;
        String ls = "(";
        String hs = ")";

        if (lowIncl) {
            aboveLow = actual.compareTo(low) >= 0;
            ls = "[";
        } else
            aboveLow = actual.compareTo(low) > 0;

        if (highIncl) {
            belowHigh = actual.compareTo(high) <= 0;
            hs = "]";
        } else
            belowHigh = actual.compareTo(high) < 0;

        within = aboveLow && belowHigh;
        // System.out.println("Compared " + actual.toString() +
        // " within " + ls + low.toString() + ", " + high.toString() + hs +
        // ": " + within);
        if (within)
            return this.report(within, testname, actual, low, high);
        else
            return this.report(within, testname
                    + "\nActual value is not within the " + ls + "low high"
                    + hs + " range.", actual, low, high);
    }

    /**
     * Test that determines whether the value of the given object (of any kind)
     * is the range between low (inclusive) and high (exclusive) values
     * 
     * @param <T>
     *            the type of the objects being compared
     * @param actual
     *            the computed value of the type T
     * @param low
     *            the lower limit of the type T (inclusive) for this range
     * @param high
     *            the upper limit of the type T (exclusive) for this range
     * @param testname
     *            the name of this test
     */

    public <T> boolean checkRange(T actual, T low, T high, Comparator<T> comp,
            String testname) {
        return checkRange(actual, low, high, true, false, comp, testname);
    }

    /**
     * Test that determines whether the value of the given object (of any kind)
     * is the range between low (inclusive) and high (exclusive) values
     * 
     * @param <T>
     *            the type of the objects being compared
     * @param actual
     *            the computed value of the type T
     * @param low
     *            the lower limit of the type T (inclusive) for this range
     * @param high
     *            the upper limit of the type T (exclusive) for this range
     */
    public <T> boolean checkRange(T actual, T low, T high, Comparator<T> comp) {
        return checkRange(actual, low, high, true, false, comp, "");
    }

    /**
     * Test that determines whether the value of the given object (of any kind)
     * is the range between low (inclusive) and high (exclusive) values
     * 
     * @param <T>
     *            the type of the objects being compared
     * @param actual
     *            the computed value of the type T
     * @param low
     *            the lower limit of the type T (inclusive) for this range
     * @param high
     *            the upper limit of the type T (exclusive) for this range
     * @param lowIncl
     *            is the low limit inclusive for this range?
     * @param highIncl
     *            is the upper limit inclusive for this range?
     * @param comp
     *            The <code>Comparator</code> used to compare the values
     */
    public <T> boolean checkRange(T actual, T low, T high, boolean lowIncl,
            boolean highIncl, Comparator<T> comp) {
        return checkRange(actual, low, high, lowIncl, highIncl, comp, "");
    }

    /**
     * Test that determines whether the value of the given object (of any kind)
     * is the range between low (inclusive) and high (exclusive) values
     * 
     * @param <T>
     *            the type of the objects being compared
     * @param actual
     *            the computed value of the type T
     * @param low
     *            the lower limit of the type T (inclusive) for this range
     * @param high
     *            the upper limit of the type T (exclusive) for this range
     * @param lowIncl
     *            is the low limit inclusive for this range?
     * @param highIncl
     *            is the upper limit inclusive for this range?
     * @param comp
     *            The <code>Comparator</code> used to compare the values
     * @param testname
     *            the name of this test
     */
    public <T> boolean checkRange(T actual, T low, T high, boolean lowIncl,
            boolean highIncl, Comparator<T> comp, String testname) {
        this.testname = testname;
        boolean within = true;
        boolean aboveLow = true;
        boolean belowHigh = true;
        String ls = "(";
        String hs = ")";

        if (lowIncl) {
            aboveLow = comp.compare(actual, low) >= 0;
            ls = "[";
        } else
            aboveLow = comp.compare(actual, low) > 0;

        if (highIncl) {
            belowHigh = comp.compare(actual, high) <= 0;
            hs = "]";
        } else
            belowHigh = comp.compare(actual, high) < 0;

        within = aboveLow && belowHigh;

        // System.out.println("Compared " + actual.toString() +
        // " within " + ls + low.toString() + ", " + high.toString() + hs +
        // ": " + within);

        if (within)
            return this.report(within, testname, actual, low, high);
        else
            return this.report(within, testname
                    + "\nActual value is not within the " + ls + "low high"
                    + hs + " range.", actual, low, high);
    }

    /*--------------------------------------------------------------------*/
    /*---------- HELPERS FOR THE TEST EVALUATION SECTION -----------------*/
    /*--------------------------------------------------------------------*/

    /**
     * Find the method with the given name for the type of the given object,
     * that consumes parameters of the given types. Account for any autoboxing
     * of primitive types
     * 
     * @param object
     *            The object expected to invoke the method
     * @param method
     *            The name of the method to invoke
     * @param parameters
     *            The parameter types for the method invocation
     * @return The instance of the declared method
     */
    private <T> Method findMethod(T object, String method,
            Class<?>[] parameters, String testname) {

        Method[] allMethods = findAllMethods(object.getClass());
        ArrayList<Method> allNamed = new ArrayList<Method>();

        // make a list of all methods with the given name
        for (Method elt : allMethods)
            if (elt.getName().equals(method))
                allNamed.add(elt);

        // / add the test to compare parameters -- invocation with int works!!
        if (allNamed.size() > 0) {
            for (Method m : allNamed) {
                if (this.matchParams(m.getParameterTypes(), parameters))
                    return m;
            }
            // no methods matched the given parameter list
            testname = testname + "\nNo method with the name " + method
                    + " had a matching argument list\n";
            return null;
        }
        // no methods found with the given name
        else {
            testname = testname + "\nNo method with the name " + method
                    + " found\n";
            return null;
        }
    }

    /**
     * Find all test methods (with the name that starts with test...) in the
     * class determined by the instance of the given object, that consumes a
     * <code>Tester</code> argument.
     * 
     * @param object
     *            The object expected to invoke the method
     * @param testname
     *            The <code>String</code> that records test diagnosis info
     * @return an <code>ArrayList</code> of all test methods
     */
    private <T> ArrayList<Method> findTestMethods(T object, String testname) {

        Method[] allMethods = findAllMethods(object.getClass());
        ArrayList<Method> allNamed = new ArrayList<Method>();
        Class<?>[] testerParam = new Class[] { this.getClass() };

        // make a list of all methods with the given name
        for (Method method : allMethods) {

            if (method.getName().startsWith("test")
                    && this.matchParams(method.getParameterTypes(),
                            testerParam)) {
                allNamed.add(method);
                method.setAccessible(true);
            } else if (method.getAnnotation(TestMethod.class) != null) {
                allNamed.add(method);
                method.setAccessible(true);
            }
        }

        System.out.println("Found " + allNamed.size() + " test methods");

        if (allNamed.size() > 0) {
            // found test methods that matched the given parameter list
            testname = testname + "Found " + allNamed.size() + " test methods";

            return allNamed;
        }
        // no test methods that matched the given parameter list
        else {
            testname = testname + "\nNo method with the name test..."
                    + " found in the class " + object.getClass().getName()
                    + "\n";
            return null;
        }
    }

    /**
     * Finds all of the methods for a particular class, including those that
     * are part of a super class. This will stop when it hits java.lang.Object.
     * While traversing the methods, it also marks all of the methods to
     * accessible so that we can utilize them elsewhere in our code. This
     * should be abstracted out to a custom reflector (similar to what we
     * already have) in the future.
     * 
     * @param c
     *            class to collect methods from
     * @return the resulting array of methods found
     */
    private Method[] findAllMethods(Class<?> c) {
        Class<?> classToSurvey = c;

        ArrayList<Method> list = new ArrayList<Method>();
        // While we have a class to examine
        while (classToSurvey != null && classToSurvey != Object.class) {
            // Get all public and private methods
            Method[] allMethods = classToSurvey.getDeclaredMethods();
            // For each method, set it to accessible and add it to the list
            for (Method m : allMethods) {
                m.setAccessible(true);
                list.add(m);
            }

            // Get the super class and loop
            classToSurvey = classToSurvey.getSuperclass();
        }

        // Convert the ArrayList to an array and return
        return list.toArray(new Method[0]);
    }

    /**
     * See if the list of <CODE>Class</CODE instances
     *   that represent the parameter list for the method to invoke
   *   matches the method definition with the same name
     * 
     * @param parInput
     *            <CODE>Array</CODE> of <CODE>Class</CODE instances
   *   that represent the input parameter list
     * @param parDefined
     *            <CODE>Array</CODE> of <CODE>Class</CODE instances
   *   that represent the parameter list for a method with matching name
     * @return true if the parameter lists represent the same classes allowing
     *         for primitive types to match their wrapper classes.
     */
    private boolean matchParams(Class<?>[] parInput, Class<?>[] parDefined) {
        if (Array.getLength(parInput) != Array.getLength(parDefined))
            return false;
        else {
            for (int i = 0; i < Array.getLength(parInput); i++) {
                String in = parInput[i].getName();
                String def = parDefined[i].getName();
                if (!in.equals(def)) {
                    if (Inspector.isWrapperClass(def)) {
                        if (!isWrapperMatch(in, def))
                            return false;
                        else
                            return true;
                    }
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check whether the primitive type with the name <CODE>in</CODE> matches
     * the wrapper type with the name <CODE>def</CODE>.
     * 
     * @param in
     *            The name of the primitive type to match
     * @param def
     *            The name of the wrapper class to match
     * @return true if the primitive type matches its wrapper class
     */
    private boolean isWrapperMatch(String in, String def) {
        if (def.equals("java.lang.Integer") && in.equals("int"))
            return true;
        else if (def.equals("java.lang.Short") && in.equals("short"))
            return true;
        else if (def.equals("java.lang.Long") && in.equals("long"))
            return true;
        else if (def.equals("java.lang.Byte") && in.equals("byte"))
            return true;
        else if (def.equals("java.lang.Character") && in.equals("char"))
            return true;
        else if (def.equals("java.lang.Double") && in.equals("double"))
            return true;
        else if (def.equals("java.lang.Float") && in.equals("float"))
            return true;
        else if (def.equals("java.lang.Double") && in.equals("double"))
            return true;
        else if (def.equals("java.lang.Boolean") && in.equals("boolean"))
            return true;
        else
            return false;
    }

    /*--------------------------------------------------------------------*/
    /*--------------------- TEST REPORTING SECTION -----------------------*/
    /*--------------------------------------------------------------------*/

    /**
     * General contractor to report test results
     * 
     * @param success
     *            Did the test succeed?
     * @param testname
     *            The name of this test
     * @param actual
     *            The computed value of the result
     * @param expected
     *            The expected value of the result
     */
    private boolean report(boolean success, String testname, Object actual,
            Object expected) {
        if (success)
            return this.reportSuccess(testname, actual, expected);
        else {
            // get the relevant stack trace for this test case
            String trace = this.getStackTrace();
            return this
                    .reportErrors(testname + "\n" + trace, actual, expected);

        }
    }

    /**
     * General contractor to report test results for range check
     * 
     * @param success
     *            Did the test succeed?
     * @param testname
     *            The name of this test
     * @param actual
     *            The computed value of the result
     * @param low
     *            The low value (inclusive)of the range
     * @param high
     *            The high value (exclusive) of the range
     */
    private boolean report(boolean success, String testname, Object actual,
            Object low, Object high) {
        if (success)
            return this.reportSuccess(testname, actual, low, high);
        else {
            // get the relevant stack trace for this test case
            String trace = this.getStackTrace();
            return this.reportErrors(testname + "\n" + trace, actual, low,
                    high);
        }
    }

    /**
     * Add a test to the list of failed tests
     * 
     * @param testname
     *            The name of the failed test
     * @param actual
     *            The computed value of the test
     * @param expected
     *            The expected value of the test
     */
    private boolean reportErrors(String testname, Object actual,
            Object expected) {

        // add test report to the error report and the full test report
        return this.addError("Error in test number " + (numberOfTests + 1)
                + "\n" + testname + "\n" + "actual:     "
                + Printer.produceString(actual) + "\n" + "expected:   "
                + Printer.produceString(expected) + "\n");
    }

    /**
     * Add a test to the list of failed tests
     * 
     * @param testname
     *            The name of the failed range test
     * @param actual
     *            The computed value of the test
     * @param low
     *            The low (inclusive) value of the range
     * @param high
     *            The high (exclusive) value of the range
     */
    private boolean reportErrors(String testname, Object actual, Object low,
            Object high) {

        // add test report to the error report and the full test report
        return this.addError("Error in range test number "
                + (numberOfTests + 1) + "\n" + testname + "\n"
                + "actual:     " + Printer.produceString(actual) + "\n"
                + "low:   " + Printer.produceString(low) + "\n" + "high:   "
                + Printer.produceString(high) + "\n");
    }

    /**
     * Produce a formatted String that represent the relevant entries of the
     * <CODE>StackTrace</CODE> that provides a link to the test case that
     * produced the error.
     * 
     * @return a formatted String representation of the relevant stack trace
     */
    private String getStackTrace() {
        try {
            // force an exception so you can inspect the stack trace
            throw new ErrorReport("Error trace:");
        } catch (ErrorReport e) { // catch the exception
            // record the original stack trace
            StackTraceElement[] ste = e.getStackTrace();

            // copy only the relevant entries
            int length = Array.getLength(ste);
            StackTraceElement[] tmpSTE = new StackTraceElement[length];
            int ui = 0; // index for the stripped stack trace
            for (int i = 3; i < length; i++) {
                String cname = ste[i].getClassName();
                if (!((cname.startsWith("tester."))
                        || (cname.startsWith("sun.reflect"))
                        || (cname.startsWith("java.lang"))
                        || (cname.startsWith("bluej")) || (cname
                        .startsWith("__SHELL")))) {
                    tmpSTE[ui] = ste[i];
                    ui = ui + 1;
                }
            }

            // we cannot have null entries at the end
            StackTraceElement[] userSTE = new StackTraceElement[ui];
            for (int i = 0; i < ui; i++) {
                userSTE[i] = tmpSTE[i];
            }

            // now set the stack trace so it can be converted to a String
            e.setStackTrace(userSTE);

            StringWriter writer = new StringWriter();
            PrintWriter printwriter = new PrintWriter(writer);
            e.printStackTrace(printwriter);

            return writer.toString();
        }
    }

    /**
     * Add a test to the list of successful tests
     * 
     * @param testname
     *            The name of the successful test
     * @param actual
     *            The computed value of the test
     * @param expected
     *            The expected value of the test
     */
    private boolean reportSuccess(String testname, Object actual,
            Object expected) {

        // add test report to the full test report
        return this.addSuccess("Success in the test number "
                + (numberOfTests + 1) + "\n" + testname + "\n"
                + "actual:     " + Printer.produceString(actual) + "\n"
                + "expected:   " + Printer.produceString(expected) + "\n");
    }

    /**
     * Add a test to the list of failed tests
     * 
     * @param testname
     *            The name of the failed range test
     * @param actual
     *            The computed value of the test
     * @param low
     *            The low (inclusive) value of the range
     * @param high
     *            The high (exclusive) value of the range
     */
    private boolean reportSuccess(String testname, Object actual, Object low,
            Object high) {

        // add test report to the full test report
        return this.addSuccess("Success in the range test number "
                + (numberOfTests + 1) + "\n" + testname + "\n"
                + "actual:     " + Printer.produceString(actual) + "\n"
                + "low:   " + Printer.produceString(low) + "\n" + "high:   "
                + Printer.produceString(high) + "\n");
    }

    /**
     * Add the given test successful test result to the full report
     * 
     * @param testResult
     *            The successful test result
     */
    private boolean addSuccess(String testResult) {
        this.numberOfTests = this.numberOfTests + 1;
        this.fullTestResults = this.fullTestResults.append("\n" + testResult);
        // update the count of all tests
        return true;
    }

    /**
     * Add the given test failed test result to the full report and to the
     * error report
     * 
     * @param testResult
     *            The failed test result
     */
    private boolean addError(String testResult) {
        // update the count of all tests
        this.numberOfTests = this.numberOfTests + 1;
        // update the count of the errors tests
        this.errors = this.errors + 1;
        this.testResults = this.testResults + "\n" + testResult;
        this.fullTestResults = this.fullTestResults.append("\n" + testResult);
        return false;
    }

    /**
     * Produce a <code>String</code> describing the number of tests that were
     * run and that failed.
     */
    private String testCount() {
        String tCount = "";
        // report test totals
        if (this.numberOfTests == 1) {
            tCount = "\nRan 1 test.\n";
        } else if (this.numberOfTests > 1) {
            tCount = "\nRan " + numberOfTests + " tests.\n";
        }

        // report error totals
        if (this.errors == 0) {
            tCount = tCount + "All tests passed.\n\n";
        }
        if (this.errors == 1) {
            tCount = tCount + "1 test failed.\n\n";

        } else if (this.errors > 1) {
            tCount = tCount + this.errors + " tests failed.\n\n";
        }

        return tCount;
    }

    /**
     * Report on the number and nature of failed tests
     */
    protected void testReport() {
        System.out.println(testCount() + this.testResults
                + "\n--- END OF TEST RESULTS ---");
    }

    /**
     * Produce test names and values compared for all tests
     */
    protected void fullTestReport() {
        System.out.println(testCount() + this.fullTestResults
                + "\n--- END OF FULL TEST RESULTS ---");
    }

    /*--------------------------------------------------------------------*/
    /*-------------- TESTER INVOCATION HELPERS SECTION -------------------*/
    /*--------------------------------------------------------------------*/

    /**
     * A hook to run the tester for any object --- needed to run from BlueJ.
     * Print all fields of the 'Examples' class and a report of all failed
     * tests.
     * 
     * @param obj
     *            The 'Examples' class instance where the tests are defined
     */
    public static void run(Object obj) {
        Tester t = new Tester();
        t.runAnyTests(obj, false, true);
    }

    /**
     * A hook to run the tester for any object and produce a full test report
     * 
     * @param obj
     *            The 'Examples' class instance where the tests are defined
     */
    public static void runFullReport(Object obj) {
        Tester t = new Tester();
        t.runAnyTests(obj, true);
    }

    /**
     * A hook to run the tester for any object and produce a full test report
     * 
     * @param obj
     *            The 'Examples' class instance where the tests are defined
     */
    public static void runReport(Object obj, boolean full, boolean printall) {
        Tester t = new Tester();
        t.runAnyTests(obj, full, printall);
    }
}