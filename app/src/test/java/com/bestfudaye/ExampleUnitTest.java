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
        StringBuffer sb = new StringBuffer("F0FE6B3D915C");
        for(int i=0;i<sb.length();i++){
            if(i%3==0){
                sb.insert(i,":");
            }
        }
        sb.delete(0,1);
        System.out.print(sb.toString());
    }
}