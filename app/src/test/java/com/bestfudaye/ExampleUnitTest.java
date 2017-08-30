package com.bestfudaye;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }
    @Test
    public void percent (){
        String string = "C0:2B:A4:97:4E:87";
        string = string.replaceAll(":","");
        System.out.print(string);
    }
}