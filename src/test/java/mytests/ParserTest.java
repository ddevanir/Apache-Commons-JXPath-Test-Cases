package mytests;

import static org.junit.Assert.*;
import org.apache.commons.jxpath.ri.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

public class ParserTest extends TestCase {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test1() {
		ParseException parseException = new ParseException();
		parseException.getMessage();
		
		String str = "India";
		assertTrue(str.equals(parseException.add_escapes(str)));
	}
	
	@Test
	public void test2() {
		ParseException parseException = new ParseException();
		parseException.getMessage();
		
		String str = "India\b";
		String str1 = "India\\b";
		assertTrue(str1.equals(parseException.add_escapes(str)));
	}
	
	@Test
	public void test3() {
		ParseException parseException = new ParseException();
		parseException.getMessage();
		
		String str = "\tIndia";
		String str1 = "\\tIndia";
		assertTrue(str1.equals(parseException.add_escapes(str)));
	}
	
	@Test
	public void test4() {
		ParseException parseException = new ParseException();
		parseException.getMessage();
		
		String str = "\nIndia";
		String str1 = "\\nIndia";
		assertTrue(str1.equals(parseException.add_escapes(str)));
	}
	
	@Test
	public void test5() {
		ParseException parseException = new ParseException();
		parseException.getMessage();
		
		String str = "India\f";
		String str1 = "India\\f";
		assertTrue(str1.equals(parseException.add_escapes(str)));
	}
	
	@Test
	public void test6() {
		ParseException parseException = new ParseException();
		parseException.getMessage();
		
		String str = "India\f";
		String str1 = "India\\f";
		assertTrue(str1.equals(parseException.add_escapes(str)));
	}
	
	@Test
	public void test7() {
		ParseException parseException = new ParseException();
		parseException.getMessage();
		
		String str = "India\r";
		String str1 = "India\\r";
		assertTrue(str1.equals(parseException.add_escapes(str)));
	}
	
	@Test
	public void test8() {
		ParseException parseException = new ParseException();
		parseException.getMessage();
		
		String str = "India\"";
		String str1 = "India\\\"";
		assertTrue(str1.equals(parseException.add_escapes(str)));
	}
	
	@Test
	public void test9() {
		ParseException parseException = new ParseException();
		parseException.getMessage();
		
		String str = "India\'";
		String str1 = "India\\\'";
		assertTrue(str1.equals(parseException.add_escapes(str)));
	}
	
	@Test
	public void test10() {
		ParseException parseException = new ParseException();
		parseException.getMessage();
		
		String str = "India\\";
		String str1 = "India\\\\";
		assertTrue(str1.equals(parseException.add_escapes(str)));
	}

}
