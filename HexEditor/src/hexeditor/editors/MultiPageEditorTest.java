package hexeditor.editors;

import static org.junit.Assert.*;

import hexeditor.editors.PluginMethods;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MultiPageEditorTest {
	Logger logger = Logger.getLogger(PluginMethods.class.getName());
	PluginMethods mp = new PluginMethods();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetTableString() {
		String st1 = "AA";
		String st2 = "AAA";
		String st3 = "41";
		try{
			Assert.assertEquals("", mp.getTableString(st1));
			Assert.assertEquals(null, mp.getTableString(st2));
			Assert.assertEquals("", mp.getTableString(st3));
		} catch (Error e) {
			logger.log(Level.SEVERE, e.getMessage());
			Assert.fail();
			}
	}

	@Test
	public void testGetFontString() {
		String st1 = "Hello!";
		String st2 = "1234567";
		try{
			Assert.assertEquals("", mp.getFontString(st1));
			Assert.assertEquals("", mp.getFontString(st2));
		} catch (Error e) {
			logger.log(Level.SEVERE, e.getMessage());
			Assert.fail();
			}
	}

	@Test
	public void testGenerateOffset() {
		String st1 = "00000210";
		String st2 = "00000f90";
		String st3 = "00000aa0";
		
		try{
			Assert.assertEquals(st1, mp.generateOffset(33));
			Assert.assertEquals(st2, mp.generateOffset(249));
			Assert.assertEquals(st3, mp.generateOffset(170));
		} catch (Error e) {
			logger.log(Level.SEVERE, e.getMessage());
			Assert.fail();
			}
	}

}

