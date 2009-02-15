package tester;

import java.lang.reflect.*;
import java.util.Set;

import tester.cobertura.AnnotationScanner;

/**
 * Copyright 2008 Viera K. Proulx, Matthias Felleisen
 * This program is distributed under the terms of the 
 * GNU Lesser General Public License (LGPL)
 */

/**
 * <P>
 * The <code>main</code> method in this class uses the command-line argument to
 * determine the class that contains the test cases. If no class name is given
 * "Examples" is assumed.
 * </P>
 * <P>
 * The main method instantiates this class and invokes the
 * <CODE>{@link Tester Tester}</CODE> on this new instance.
 * </P>
 * 
 * @author Viera K. Proulx, Matthias Felleisen, Weston jossey
 * @since 3 March 2008, 16 October 2008, 20 December 2008
 * 
 */
public class Main {
	/**
	 * Method that creates an instance of the <code>Tester</code> and an
	 * instance of the class that defines the tests, invokes the test evaluation
	 * by the <code>Tester</code>, and reports the results.
	 * 
	 * @param argv
	 *            [optional] the name of the class that defines the tests
	 * @throws Exception 
	 */
	public static void main(String argv[]) throws Exception {
		//pool = Executors.newCachedThreadPool(); //For concurrency
		//Instrumentor inst = new Instrumentor();
		//Set<String> instrumentedClasses = new AnnotationScanner(tester.cobertura.Instrument.class).scan();
		
		//if(instrumentedClasses != null){
		//	inst.instrumentClasses();
		//}
		
		String classname;
		if ((argv == null) || (Array.getLength(argv) == 0))
			classname = "Examples";
		else
			classname = argv[0];

		AnnotationScanner scanner = new AnnotationScanner(
				tester.Example.class);
		Set<String> classes = null;
		
		//Needs better comments.
		try {
			classes = scanner.scan();
			
			// get the name of the class that defines the tests
			if(classes != null){
				for (String clazz : classes){
					new AnnotatedTest(clazz).run();
					System.out.println(clazz);
					//pool.execute(new AnnotatedTest(clazz));
				}
			}
		} catch (Exception e) {
			System.err.println("Unable to scan for the annotated classes.\n" +
					"Shutting down...");
			e.printStackTrace();
		}finally{

			//if(instrumentedClasses != null){
			//	inst.generateReports();
			//}			
			
		}

		if(classes == null){
			System.out.println("Tester Results");

			// construct an instance of the class that defines the tests
			Object o = null;

			Class<?> examples;
			try {
				examples = Class.forName(classname);

				try {
					Constructor<?> construct = examples
							.getDeclaredConstructor();
					construct.setAccessible(true);
					o = construct.newInstance();

					System.out.println("Tester Results");
				} catch (NoSuchMethodException ex) {
					System.out.println("no default costructor: "
							+ ex.getMessage());
				} catch (InvocationTargetException ex) {
					System.out.println("Invocation: " + ex.getMessage());
				}
				// run tests if the instance was successfully constructed
				if (o != null) {
					
					//if(instrumentedClasses != null){
					//	inst.instrumentClasses();
					//}
					
					Tester t = new Tester();
					t.runAnyTests(o);
					
					//if(instrumentedClasses != null){
					//	inst.generateReports();
					//}
					

				}

				// report the problem if an instance of the class that defines the
				// tests
				// has not been successfully constructed
				else
					throw new RuntimeException("The examples class could not be instantiated.\n" +
							"Please check to make sure that your examples class has a default " +
							"constructor that does not fail during instantiation.");
			}
			// report errors:
			// wrong classname:
			catch (InstantiationException c) {
				System.err.println("Cannot construct an instance of "
						+ classname);
				//System.out.println("Cannot construct an instance of "
				//		+ classname);
			} catch (IllegalAccessException c) {
				System.err.println(c.getLocalizedMessage());
			} catch (ClassNotFoundException c) {
				System.err.println(classname + " class doesn't exist");
				//System.out.println(classname + " class doesn't exist");
			} catch (ClassCastException c) {
				System.err.println("Examples is expected to extend Main");
				//System.out.println("Examples is expected to extend Main");
			}
			// finish up
			finally {
				System.out.println(" ");
			}
		}
		
		System.exit(0); //Some bug is causing the threads not to terminate
	}
}
